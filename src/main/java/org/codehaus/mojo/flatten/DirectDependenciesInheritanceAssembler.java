/**
 * 
 */
package org.codehaus.mojo.flatten;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Specializes;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.inheritance.DefaultInheritanceAssembler;
import org.apache.maven.model.merge.MavenModelMerger;

/**
 * @author kemalsoysal
 *
 */
@Specializes
@Singleton
@Named
public class DirectDependenciesInheritanceAssembler extends DefaultInheritanceAssembler {

	protected InheritanceModelMerger merger = new DirectDependenciesInheritanceModelMerger();

	/**
	 * copied from super implementation because it is private
	 */
	private static final String CHILD_DIRECTORY = "child-directory";

	/**
	 * copied from super implementation because it is private
	 */
	private static final String CHILD_DIRECTORY_PROPERTY = "project.directory";

	protected FlattenDependencyMode flattenDependencyMode;
	
	/**
	 * 
	 */
	public DirectDependenciesInheritanceAssembler() {
	}

	@Override
	public void assembleModelInheritance(Model child, Model parent, ModelBuildingRequest request,
			ModelProblemCollector problems) {
		Map<Object, Object> hints = new HashMap<>();
		String childPath = child.getProperties().getProperty(CHILD_DIRECTORY_PROPERTY, child.getArtifactId());
		hints.put(CHILD_DIRECTORY, childPath);
		hints.put(MavenModelMerger.CHILD_PATH_ADJUSTMENT, getChildPathAdjustment(child, parent, childPath));
		merger.merge(child, parent, false, hints);
	}

	/**
	 * copied from super implementation because it is private though the adjustment
	 * is only for compatibility due to the comment with Maven 2.0
	 * 
	 * @param child
	 * @param parent
	 * @param childDirectory
	 * @return
	 */
	private String getChildPathAdjustment(Model child, Model parent, String childDirectory) {
		String adjustment = "";

		if (parent != null) {
			String childName = child.getArtifactId();

			/*
			 * This logic (using filesystem, against wanted independence from the user
			 * environment) exists only for the sake of backward-compat with 2.x (MNG-5000).
			 * In general, it is wrong to base URL inheritance on the module directory names
			 * as this information is unavailable for POMs in the repository. In other
			 * words, modules where artifactId != moduleDirName will see different effective
			 * URLs depending on how the model was constructed (from filesystem or from
			 * repository).
			 */
			if (child.getProjectDirectory() != null) {
				childName = child.getProjectDirectory().getName();
			}

			for (String module : parent.getModules()) {
				module = module.replace('\\', '/');

				if (module.regionMatches(true, module.length() - 4, ".xml", 0, 4)) {
					module = module.substring(0, module.lastIndexOf('/') + 1);
				}

				String moduleName = module;
				if (moduleName.endsWith("/")) {
					moduleName = moduleName.substring(0, moduleName.length() - 1);
				}

				int lastSlash = moduleName.lastIndexOf('/');

				moduleName = moduleName.substring(lastSlash + 1);

				if ((moduleName.equals(childName) || (moduleName.equals(childDirectory))) && lastSlash >= 0) {
					adjustment = module.substring(0, lastSlash);
					break;
				}
			}
		}

		return adjustment;
	}

	/**
	 * InheritanceModelMerger
	 */
	protected class DirectDependenciesInheritanceModelMerger
			extends DefaultInheritanceAssembler.InheritanceModelMerger {

		@Override
		public void merge(Model target, Model source, boolean sourceDominant, Map<?, ?> hints) {
			super.merge(target, source, sourceDominant, hints);
		}

		@Override
		protected void mergeModelBase_Dependencies(ModelBase target, ModelBase source, boolean sourceDominant,
				Map<Object, Object> context) {
			if(flattenDependencyMode == null || flattenDependencyMode == FlattenDependencyMode.direct) {
				return;
			}
			super.mergeModelBase_Dependencies(target, source, sourceDominant, context);
		}
	}
}
