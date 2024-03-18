package org.codehaus.mojo.flatten.extendedinterpolation;

import java.io.File;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.interpolation.ModelInterpolator;

public interface ExtendedModelInterpolator extends ModelInterpolator {
    Model interpolateModel(
            Model effectiveModel,
            Model model,
            File projectDir,
            ModelBuildingRequest config,
            ModelProblemCollector problems);
}
