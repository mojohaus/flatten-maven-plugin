package org.codehaus.mojo.flatten.extendedinterpolation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.apache.maven.model.interpolation.DefaultModelVersionProcessor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExtendedStringSearchModelInterpolatorTest {

    @Test
    public void testExtendedStringSearchModelInterpolator() throws Exception {
        ExtendedStringSearchModelInterpolator interpolator = new ExtendedStringSearchModelInterpolator();
        interpolator.setVersionPropertiesProcessor(new DefaultModelVersionProcessor());

        Model efectiveModel = new Model();
        efectiveModel.addProperty("foo", "bar");

        Model model = new Model();
        model.setArtifactId("${foo}");
        model.setDescription("${basedir}");

        List<ModelProblemCollectorRequest> problems = new ArrayList<>();

        interpolator.interpolateModel(
                efectiveModel, model, new File("."), new DefaultModelBuildingRequest(), problems::add);

        assertEquals(0, problems.size());
        assertEquals("bar", model.getArtifactId());
        assertEquals("${basedir}", model.getDescription());
    }
}
