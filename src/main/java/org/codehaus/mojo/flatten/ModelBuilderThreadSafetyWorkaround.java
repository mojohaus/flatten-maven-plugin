package org.codehaus.mojo.flatten;

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

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.model.composition.DependencyManagementImporter;
import org.apache.maven.model.interpolation.ModelInterpolator;
import org.apache.maven.model.management.DependencyManagementInjector;
import org.apache.maven.model.management.PluginManagementInjector;
import org.apache.maven.model.normalization.ModelNormalizer;
import org.apache.maven.model.path.ModelPathTranslator;
import org.apache.maven.model.path.ModelUrlNormalizer;
import org.apache.maven.model.plugin.LifecycleBindingsInjector;
import org.apache.maven.model.plugin.PluginConfigurationExpander;
import org.apache.maven.model.plugin.ReportConfigurationExpander;
import org.apache.maven.model.plugin.ReportingConverter;
import org.apache.maven.model.profile.ProfileInjector;
import org.apache.maven.model.profile.ProfileSelector;
import org.apache.maven.model.superpom.SuperPomProvider;
import org.apache.maven.model.validation.ModelValidator;
import org.eclipse.sisu.Nullable;

/**
 * Works around thread safety issues when modifying the global singleton {@link org.apache.maven.model.building.DefaultModelBuilder DefaultModelBuilder}
 * with custom {@link ProfileInjector} and {@link ProfileSelector}. Instead of modifying the global {@code DefaultModelBuilder}, this class
 * creates a new {@code DefaultModelBuilder} and equips it with the currently active components like {@link ModelProcessor}, {@link ModelValidator} etc.
 * which might have been modified/provided by other Maven extensions. 
 * 
 * @author Falko Modler
 * @since 1.2.3
 */
@Named
public class ModelBuilderThreadSafetyWorkaround
{

    @Inject
    private ModelProcessor modelProcessor;

    @Inject
    private ModelValidator modelValidator;

    @Inject
    private ModelNormalizer modelNormalizer;

    @Inject
    private ModelInterpolator modelInterpolator;

    @Inject
    private ModelPathTranslator modelPathTranslator;

    @Inject
    private ModelUrlNormalizer modelUrlNormalizer;

    @Inject
    private SuperPomProvider superPomProvider;

    @Inject
    private DirectDependenciesInheritanceAssembler inheritanceAssembler;

    @Inject
    private PluginManagementInjector pluginManagementInjector;

    @Inject
    private DependencyManagementInjector dependencyManagementInjector;

    @Inject
    private DependencyManagementImporter dependencyManagementImporter;

    @Inject
    @Nullable
    private LifecycleBindingsInjector lifecycleBindingsInjector;

    @Inject
    private PluginConfigurationExpander pluginConfigurationExpander;

    @Inject
    private ReportConfigurationExpander reportConfigurationExpander;

    @Inject
    private ReportingConverter reportingConverter;

    public ModelBuildingResult build( ModelBuildingRequest buildingRequest, ProfileInjector customInjector, ProfileSelector customSelector )
        throws ModelBuildingException
    {
        // note: there is neither DefaultModelBuilder.get*(), nor DefaultModelBuilder.clone()
        return new DefaultModelBuilderFactory().newInstance()
            .setProfileInjector( customInjector )
            .setProfileSelector( customSelector )
            // apply currently active ModelProcessor etc. to support extensions like jgitver
            .setDependencyManagementImporter( dependencyManagementImporter )
            .setDependencyManagementInjector( dependencyManagementInjector )
            .setInheritanceAssembler( inheritanceAssembler )
            .setLifecycleBindingsInjector( lifecycleBindingsInjector )
            .setModelInterpolator( modelInterpolator )
            .setModelNormalizer( modelNormalizer )
            .setModelPathTranslator( modelPathTranslator )
            .setModelProcessor( modelProcessor )
            .setModelUrlNormalizer( modelUrlNormalizer )
            .setModelValidator( modelValidator )
            .setPluginConfigurationExpander( pluginConfigurationExpander )
            .setPluginManagementInjector( pluginManagementInjector )
            .setReportConfigurationExpander( reportConfigurationExpander )
            .setReportingConverter( reportingConverter )
            .setSuperPomProvider( superPomProvider )
            .build( buildingRequest );
    }
}
