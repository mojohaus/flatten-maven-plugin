package org.codehaus.mojo.flatten.extendedinterpolation;

import javax.inject.Named;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.interpolation.StringVisitorModelInterpolator;
import org.codehaus.plexus.interpolation.ValueSource;

@Named
public class ExtendedStringSearchModelInterpolator extends StringVisitorModelInterpolator
        implements ExtendedModelInterpolator {

    private static final List<String> NOT_INTERPOLATABLES = Stream.of(
                    "basedir",
                    "baseUri",
                    "build.directory",
                    "build.outputDirectory",
                    "build.sourceDirectory",
                    "build.scriptSourceDirectory",
                    "build.testSourceDirectory",
                    "reporting.outputDirectory")
            .flatMap(suffix -> Stream.of(suffix, "pom." + suffix, "project." + suffix))
            .collect(Collectors.toList());

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Model> valueSourceOriginModel = Optional.empty();

    public ExtendedStringSearchModelInterpolator() {
        FilteringValueSourceWrapper.setClassLoader(getClass().getSuperclass().getClassLoader());
    }

    @Override
    protected List<ValueSource> createValueSources(
            Model model, File projectDir, ModelBuildingRequest config, ModelProblemCollector problems) {

        if (valueSourceOriginModel.isPresent()) {
            return FilteringValueSourceWrapper.wrap(
                    super.createValueSources(this.valueSourceOriginModel.get(), projectDir, config, problems),
                    this::interpolatable);
        }
        return super.createValueSources(model, projectDir, config, problems);
    }

    private boolean interpolatable(String expression) {

        return !NOT_INTERPOLATABLES.contains(expression);
    }

    @Override
    public Model interpolateModel(
            Model valueSourceOriginModel,
            Model model,
            File projectDir,
            ModelBuildingRequest config,
            ModelProblemCollector problems) {
        if (valueSourceOriginModel == null) {
            throw new IllegalArgumentException("effectiveModel is null");
        }

        if (model == null) {
            throw new IllegalArgumentException("model is null");
        }

        this.valueSourceOriginModel = Optional.of(valueSourceOriginModel);
        try {
            return super.interpolateModel(model, projectDir, config, problems);
        } finally {
            this.valueSourceOriginModel = Optional.empty();
        }
    }
}
