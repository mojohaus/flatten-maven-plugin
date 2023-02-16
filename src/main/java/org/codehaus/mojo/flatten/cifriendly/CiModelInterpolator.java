package org.codehaus.mojo.flatten.cifriendly;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.apache.maven.model.building.ModelProblem.Version;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.apache.maven.model.interpolation.MavenBuildTimestamp;
import org.apache.maven.model.interpolation.ModelInterpolator;
import org.apache.maven.model.path.PathTranslator;
import org.apache.maven.model.path.UrlNormalizer;
import org.codehaus.plexus.interpolation.AbstractValueSource;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.InterpolationPostProcessor;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.ObjectBasedValueSource;
import org.codehaus.plexus.interpolation.PrefixAwareRecursionInterceptor;
import org.codehaus.plexus.interpolation.PrefixedObjectValueSource;
import org.codehaus.plexus.interpolation.PrefixedValueSourceWrapper;
import org.codehaus.plexus.interpolation.RecursionInterceptor;
import org.codehaus.plexus.interpolation.ValueSource;
import org.codehaus.plexus.interpolation.util.ValueSourceUtils;

/**
 * Based on StringSearchModelInterpolator in maven-model-builder.
 *
 * IMPORTANT: this is a legacy Plexus component, with manually authored descriptor in src/main/resources!
 */
public class CiModelInterpolator implements CiInterpolator, ModelInterpolator
{

    private static final List<String> PROJECT_PREFIXES = Arrays.asList( "pom.", "project." );

    private static final Collection<String> TRANSLATED_PATH_EXPRESSIONS;

    private static final Map<Class<?>, InterpolateObjectAction.CacheItem> CACHED_ENTRIES = new ConcurrentHashMap<>(
            80, 0.75f, 2 );
    // Empirical data from 3.x, actual =40

    static
    {
        Collection<String> translatedPrefixes = new HashSet<>();

        // MNG-1927, MNG-2124, MNG-3355:
        // If the build section is present and the project directory is
        // non-null, we should make
        // sure interpolation of the directories below uses translated paths.
        // Afterward, we'll double back and translate any paths that weren't
        // covered during interpolation via the
        // code below...
        translatedPrefixes.add( "build.directory" );
        translatedPrefixes.add( "build.outputDirectory" );
        translatedPrefixes.add( "build.testOutputDirectory" );
        translatedPrefixes.add( "build.sourceDirectory" );
        translatedPrefixes.add( "build.testSourceDirectory" );
        translatedPrefixes.add( "build.scriptSourceDirectory" );
        translatedPrefixes.add( "reporting.outputDirectory" );

        TRANSLATED_PATH_EXPRESSIONS = translatedPrefixes;
    }

    private final Interpolator interpolator;

    // There is a protected setter for this one?
    private RecursionInterceptor recursionInterceptor;

    private PathTranslator pathTranslator;

    private UrlNormalizer urlNormalizer;

    public CiModelInterpolator()
    {
        this.interpolator = createInterpolator();
        this.recursionInterceptor = new PrefixAwareRecursionInterceptor( PROJECT_PREFIXES );
    }

    public Model interpolateModel( Model model, File projectDir, ModelBuildingRequest config,
                                   ModelProblemCollector problems )
    {
        interpolateObject( model, model, projectDir, config, problems );

        return model;
    }

    protected void interpolateObject( Object obj, Model model, File projectDir, ModelBuildingRequest config,
                                      ModelProblemCollector problems )
    {
        try
        {
            List<? extends ValueSource> valueSources = createValueSources( model, projectDir, config, problems );
            List<? extends InterpolationPostProcessor> postProcessors =
                createPostProcessors( model, projectDir, config );

            new InterpolateObjectAction( obj, valueSources, postProcessors, this, problems ).run();
        }
        finally
        {
            getInterpolator().clearAnswers();
        }
    }

    protected String interpolateInternal( String src, List<? extends ValueSource> valueSources,
                                          List<? extends InterpolationPostProcessor> postProcessors,
                                          ModelProblemCollector problems )
    {
        if ( src != null && !src.contains( "${revision}" ) && !src.contains( "${sha1}" ) && !src.contains(
            "${changelist}" ) )
        {
            return src;
        }

        String result = src;
        synchronized ( this )
        {

            for ( ValueSource vs : valueSources )
            {
                getInterpolator().addValueSource( vs );
            }

            for ( InterpolationPostProcessor postProcessor : postProcessors )
            {
                getInterpolator().addPostProcessor( postProcessor );
            }

            try
            {
                try
                {
                    result = getInterpolator().interpolate( result, getRecursionInterceptor() );
                }
                catch ( InterpolationException e )
                {
                    problems.add( new ModelProblemCollectorRequest( Severity.ERROR, Version.BASE )
                                      .setMessage( e.getMessage() ).setException( e ) );
                }

                getInterpolator().clearFeedback();
            }
            finally
            {
                for ( ValueSource vs : valueSources )
                {
                    getInterpolator().removeValuesSource( vs );
                }

                for ( InterpolationPostProcessor postProcessor : postProcessors )
                {
                    getInterpolator().removePostProcessor( postProcessor );
                }
            }
        }

        return result;
    }

    protected Interpolator createInterpolator()
    {
        CiInterpolatorImpl interpolator = new CiInterpolatorImpl();
        interpolator.setCacheAnswers( true );

        return interpolator;
    }

    private static final class InterpolateObjectAction implements Runnable
    {

        private final LinkedList<Object> interpolationTargets;

        private final CiModelInterpolator modelInterpolator;

        private final List<? extends ValueSource> valueSources;

        private final List<? extends InterpolationPostProcessor> postProcessors;

        private final ModelProblemCollector problems;

        InterpolateObjectAction( Object target, List<? extends ValueSource> valueSources,
                                        List<? extends InterpolationPostProcessor> postProcessors,
                                        CiModelInterpolator modelInterpolator,
                                        ModelProblemCollector problems )
        {
            this.valueSources = valueSources;
            this.postProcessors = postProcessors;

            this.interpolationTargets = new LinkedList<>();
            interpolationTargets.add( target );

            this.modelInterpolator = modelInterpolator;

            this.problems = problems;
        }

        @Override
        public void run()
        {
            while ( !interpolationTargets.isEmpty() )
            {
                Object obj = interpolationTargets.removeFirst();

                traverseObjectWithParents( obj.getClass(), obj );
            }
        }

        private String interpolate( String value )
        {
            return modelInterpolator.interpolateInternal( value, valueSources, postProcessors, problems );
        }

        private void traverseObjectWithParents( Class<?> cls, Object target )
        {
            if ( cls == null )
            {
                return;
            }

            CacheItem cacheEntry = getCacheEntry( cls );
            if ( cacheEntry.isArray() )
            {
                evaluateArray( target, this );
            }
            else if ( cacheEntry.isQualifiedForInterpolation )
            {
                cacheEntry.interpolate( target, this );

                traverseObjectWithParents( cls.getSuperclass(), target );
            }
        }

        private CacheItem getCacheEntry( Class<?> cls )
        {
            CacheItem cacheItem = CACHED_ENTRIES.get( cls );
            if ( cacheItem == null )
            {
                cacheItem = new CacheItem( cls );
                CACHED_ENTRIES.put( cls, cacheItem );
            }
            return cacheItem;
        }

        private static void evaluateArray( Object target, InterpolateObjectAction ctx )
        {
            int len = Array.getLength( target );
            for ( int i = 0; i < len; i++ )
            {
                Object value = Array.get( target, i );
                if ( value != null )
                {
                    if ( String.class == value.getClass() )
                    {
                        String interpolated = ctx.interpolate( (String) value );

                        if ( !interpolated.equals( value ) )
                        {
                            Array.set( target, i, interpolated );
                        }
                    }
                    else
                    {
                        ctx.interpolationTargets.add( value );
                    }
                }
            }
        }

        private static class CacheItem
        {
            private final boolean isArray;

            private final boolean isQualifiedForInterpolation;

            private final CacheField[] fields;

            private boolean isQualifiedForInterpolation( Class<?> cls )
            {
                return !cls.getName().startsWith( "java" );
            }

            private boolean isQualifiedForInterpolation( Field field, Class<?> fieldType )
            {
                if ( Map.class.equals( fieldType ) && "locations".equals( field.getName() ) )
                {
                    return false;
                }

                // noinspection SimplifiableIfStatement
                if ( fieldType.isPrimitive() )
                {
                    return false;
                }

                return !"parent".equals( field.getName() );
            }

            CacheItem( Class clazz )
            {
                this.isQualifiedForInterpolation = isQualifiedForInterpolation( clazz );
                this.isArray = clazz.isArray();
                List<CacheField> fields = new ArrayList<>();
                for ( Field currentField : clazz.getDeclaredFields() )
                {
                    Class<?> type = currentField.getType();
                    if ( isQualifiedForInterpolation( currentField, type ) )
                    {
                        if ( String.class == type )
                        {
                            if ( !Modifier.isFinal( currentField.getModifiers() ) )
                            {
                                fields.add( new StringField( currentField ) );
                            }
                        }
                        else if ( List.class.isAssignableFrom( type ) )
                        {
                            fields.add( new ListField( currentField ) );
                        }
                        else if ( Collection.class.isAssignableFrom( type ) )
                        {
                            throw new RuntimeException( "We dont interpolate into collections, use a list instead" );
                        }
                        else if ( Map.class.isAssignableFrom( type ) )
                        {
                            fields.add( new MapField( currentField ) );
                        }
                        else
                        {
                            fields.add( new ObjectField( currentField ) );
                        }
                    }

                }
                this.fields = fields.toArray( new CacheField[0] );

            }

            public void interpolate( Object target, InterpolateObjectAction interpolateObjectAction )
            {
                for ( CacheField field : fields )
                {
                    field.interpolate( target, interpolateObjectAction );
                }
            }

            public boolean isArray()
            {
                return isArray;
            }
        }

        abstract static class CacheField
        {
            protected final Field field;

            CacheField( Field field )
            {
                this.field = field;
            }

            void interpolate( Object target, InterpolateObjectAction interpolateObjectAction )
            {
                synchronized ( field )
                {
                    boolean isAccessible = field.isAccessible();
                    field.setAccessible( true );
                    try
                    {
                        doInterpolate( target, interpolateObjectAction );
                    }
                    catch ( IllegalArgumentException e )
                    {
                        interpolateObjectAction.problems
                            .add( new ModelProblemCollectorRequest( Severity.ERROR, Version.BASE )
                                      .setMessage( "Failed to interpolate field3: " + field + " on class: "
                                                       + field.getType().getName() )
                                      .setException( e ) ); // todo: Not entirely
                        // the same message
                    }
                    catch ( IllegalAccessException e )
                    {
                        interpolateObjectAction.problems
                            .add( new ModelProblemCollectorRequest( Severity.ERROR, Version.BASE )
                                      .setMessage( "Failed to interpolate field4: " + field + " on class: "
                                                       + field.getType().getName() )
                                      .setException( e ) );
                    }
                    finally
                    {
                        field.setAccessible( isAccessible );
                    }
                }

            }

            abstract void doInterpolate( Object target, InterpolateObjectAction ctx ) throws IllegalAccessException;
        }

        static final class StringField extends CacheField
        {
            StringField( Field field )
            {
                super( field );
            }

            @Override
            void doInterpolate( Object target, InterpolateObjectAction ctx ) throws IllegalAccessException
            {
                String value = (String) field.get( target );
                if ( value == null )
                {
                    return;
                }

                String interpolated = ctx.interpolate( value );

                if ( !interpolated.equals( value ) )
                {
                    field.set( target, interpolated );
                }
            }
        }

        static final class ListField extends CacheField
        {
            ListField( Field field )
            {
                super( field );
            }

            @Override
            void doInterpolate( Object target, InterpolateObjectAction ctx ) throws IllegalAccessException
            {
                @SuppressWarnings( "unchecked" )
                List<Object> c = (List<Object>) field.get( target );
                if ( c == null )
                {
                    return;
                }

                int size = c.size();
                Object value;
                for ( int i = 0; i < size; i++ )
                {

                    value = c.get( i );

                    if ( value != null )
                    {
                        if ( String.class == value.getClass() )
                        {
                            String interpolated = ctx.interpolate( (String) value );

                            if ( !interpolated.equals( value ) )
                            {
                                try
                                {
                                    c.set( i, interpolated );
                                }
                                catch ( UnsupportedOperationException e )
                                {
                                    return;
                                }
                            }
                        }
                        else
                        {
                            if ( value.getClass().isArray() )
                            {
                                evaluateArray( value, ctx );
                            }
                            else
                            {
                                ctx.interpolationTargets.add( value );
                            }
                        }
                    }
                }
            }
        }

        static final class MapField extends CacheField
        {
            MapField( Field field )
            {
                super( field );
            }

            @Override
            void doInterpolate( Object target, InterpolateObjectAction ctx ) throws IllegalAccessException
            {
                @SuppressWarnings( "unchecked" )
                Map<Object, Object> m = (Map<Object, Object>) field.get( target );
                if ( m == null || m.isEmpty() )
                {
                    return;
                }

                for ( Map.Entry<Object, Object> entry : m.entrySet() )
                {
                    Object value = entry.getValue();

                    if ( value == null )
                    {
                        continue;
                    }

                    if ( String.class == value.getClass() )
                    {
                        String interpolated = ctx.interpolate( (String) value );

                        if ( !interpolated.equals( value ) )
                        {
                            try
                            {
                                entry.setValue( interpolated );
                            }
                            catch ( UnsupportedOperationException ignore )
                            {
                                // nop
                            }
                        }
                    }
                    else if ( value.getClass().isArray() )
                    {
                        evaluateArray( value, ctx );
                    }
                    else
                    {
                        ctx.interpolationTargets.add( value );
                    }
                }
            }
        }

        static final class ObjectField extends CacheField
        {
            private final boolean isArray;

            ObjectField( Field field )
            {
                super( field );
                this.isArray = field.getType().isArray();
            }

            @Override
            void doInterpolate( Object target, InterpolateObjectAction ctx ) throws IllegalAccessException
            {
                Object value = field.get( target );
                if ( value != null )
                {
                    if ( isArray )
                    {
                        evaluateArray( value, ctx );
                    }
                    else
                    {
                        ctx.interpolationTargets.add( value );
                    }
                }
            }
        }

    }

    protected List<ValueSource> createValueSources( final Model model, final File projectDir,
                                                    final ModelBuildingRequest config,
                                                    final ModelProblemCollector problems )
    {
        Properties modelProperties = model.getProperties();

        ValueSource modelValueSource1 = new PrefixedObjectValueSource( PROJECT_PREFIXES, model, false );
        if ( config.getValidationLevel() >= ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_2_0 )
        {
            modelValueSource1 = new ProblemDetectingValueSource( modelValueSource1, "pom.", "project.", problems );
        }

        ValueSource modelValueSource2 = new ObjectBasedValueSource( model );
        if ( config.getValidationLevel() >= ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_2_0 )
        {
            modelValueSource2 = new ProblemDetectingValueSource( modelValueSource2, "", "project.", problems );
        }

        // NOTE: Order counts here!
        List<ValueSource> valueSources = new ArrayList<>( 9 );

        if ( projectDir != null )
        {
            ValueSource basedirValueSource = new PrefixedValueSourceWrapper( new AbstractValueSource( false )
            {
                public Object getValue( String expression )
                {
                    if ( "basedir".equals( expression ) )
                    {
                        return projectDir.getAbsolutePath();
                    }
                    return null;
                }
            }, PROJECT_PREFIXES, true );
            valueSources.add( basedirValueSource );

            ValueSource baseUriValueSource = new PrefixedValueSourceWrapper( new AbstractValueSource( false )
            {
                public Object getValue( String expression )
                {
                    if ( "baseUri".equals( expression ) )
                    {
                        return projectDir.getAbsoluteFile().toURI().toString();
                    }
                    return null;
                }
            }, PROJECT_PREFIXES, false );
            valueSources.add( baseUriValueSource );
            valueSources.add( new BuildTimestampValueSource( config.getBuildStartTime(), modelProperties ) );
        }

        valueSources.add( modelValueSource1 );

        valueSources.add( new MapBasedValueSource( config.getUserProperties() ) );

        valueSources.add( new MapBasedValueSource( modelProperties ) );

        valueSources.add( new MapBasedValueSource( config.getSystemProperties() ) );

        valueSources.add( new AbstractValueSource( false )
        {
            public Object getValue( String expression )
            {
                return config.getSystemProperties().getProperty( "env." + expression );
            }
        } );

        valueSources.add( modelValueSource2 );

        return valueSources;
    }

    protected List<? extends InterpolationPostProcessor> createPostProcessors( final Model model, final File projectDir,
                                                                               final ModelBuildingRequest config )
    {
        List<InterpolationPostProcessor> processors = new ArrayList<>( 2 );
        if ( projectDir != null )
        {
            processors.add( new PathTranslatingPostProcessor( PROJECT_PREFIXES, TRANSLATED_PATH_EXPRESSIONS, projectDir,
                                                              pathTranslator ) );
        }
        processors.add( new UrlNormalizingPostProcessor( urlNormalizer ) );
        return processors;
    }

    protected RecursionInterceptor getRecursionInterceptor()
    {
        return recursionInterceptor;
    }

    protected void setRecursionInterceptor( RecursionInterceptor recursionInterceptor )
    {
        this.recursionInterceptor = recursionInterceptor;
    }

    protected final Interpolator getInterpolator()
    {
        return interpolator;
    }

    class BuildTimestampValueSource extends AbstractValueSource
    {
        private final MavenBuildTimestamp mavenBuildTimestamp;

        BuildTimestampValueSource( Date startTime, Properties properties )
        {
            super( false );
            this.mavenBuildTimestamp = new MavenBuildTimestamp( startTime, properties );
        }

        public Object getValue( String expression )
        {
            if ( "build.timestamp".equals( expression ) || "maven.build.timestamp".equals( expression ) )
            {
                return mavenBuildTimestamp.formattedTimestamp();
            }
            return null;
        }
    }

    class ProblemDetectingValueSource implements ValueSource
    {

        private final ValueSource valueSource;

        private final String bannedPrefix;

        private final String newPrefix;

        private final ModelProblemCollector problems;

        ProblemDetectingValueSource( ValueSource valueSource, String bannedPrefix, String newPrefix,
                                            ModelProblemCollector problems )
        {
            this.valueSource = valueSource;
            this.bannedPrefix = bannedPrefix;
            this.newPrefix = newPrefix;
            this.problems = problems;
        }

        public Object getValue( String expression )
        {
            Object value = valueSource.getValue( expression );

            if ( value != null && expression.startsWith( bannedPrefix ) )
            {
                String msg = "The expression ${" + expression + "} is deprecated.";
                if ( newPrefix != null && newPrefix.length() > 0 )
                {
                    msg += " Please use ${" + newPrefix + expression.substring( bannedPrefix.length() ) + "} instead.";
                }
                problems.add( new ModelProblemCollectorRequest( Severity.WARNING, Version.V20 ).setMessage( msg ) );
            }

            return value;
        }

        @SuppressWarnings( "unchecked" )
        public List getFeedback()
        {
            return valueSource.getFeedback();
        }

        public void clearFeedback()
        {
            valueSource.clearFeedback();
        }

    }

    class PathTranslatingPostProcessor implements InterpolationPostProcessor
    {

        private final Collection<String> unprefixedPathKeys;
        private final File projectDir;
        private final PathTranslator pathTranslator;
        private final List<String> expressionPrefixes;

        PathTranslatingPostProcessor( List<String> expressionPrefixes, Collection<String> unprefixedPathKeys,
                                             File projectDir, PathTranslator pathTranslator )
        {
            this.expressionPrefixes = expressionPrefixes;
            this.unprefixedPathKeys = unprefixedPathKeys;
            this.projectDir = projectDir;
            this.pathTranslator = pathTranslator;
        }

        public Object execute( String expression, Object value )
        {
            if ( value != null )
            {
                expression = ValueSourceUtils.trimPrefix( expression, expressionPrefixes, true );

                if ( unprefixedPathKeys.contains( expression ) )
                {
                    return pathTranslator.alignToBaseDirectory( String.valueOf( value ), projectDir );
                }
            }

            return null;
        }

    }

    class UrlNormalizingPostProcessor implements InterpolationPostProcessor
    {


        private UrlNormalizer normalizer;

        UrlNormalizingPostProcessor( UrlNormalizer normalizer )
        {
            this.normalizer = normalizer;
        }

        public Object execute( String expression, Object value )
        {
            Set<String> expressions = new HashSet<>();
            expressions.add( "project.url" );
            expressions.add( "project.scm.url" );
            expressions.add( "project.scm.connection" );
            expressions.add( "project.scm.developerConnection" );
            expressions.add( "project.distributionManagement.site.url" );

            if ( value != null && expressions.contains( expression ) )
            {
                return normalizer.normalize( value.toString() );
            }

            return null;
        }

    }

}
