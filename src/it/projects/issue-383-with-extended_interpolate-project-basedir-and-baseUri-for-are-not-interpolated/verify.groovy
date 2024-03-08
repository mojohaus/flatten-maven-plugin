/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import groovy.xml.XmlSlurper


{
    final File pom = new File(basedir, 'pom.xml')
    assert pom.exists()

    final pomOriginal = new XmlSlurper().parse(pom)

    assert 'x${project.artifactId}y' == pomOriginal.properties.projectArtifactId.text()
    assert 'x${project.groupId}y' == pomOriginal.properties.projectGroupId.text()
    assert 'x${project.version}y' == pomOriginal.properties.projectVersion.text()
    assert 'x${project.parent.artifactId}y' == pomOriginal.properties.projectParentArtifactId.text()
    assert 'x${project.parent.groupId}y' == pomOriginal.properties.projectParentGroupId.text()
    assert 'x${project.build.directory}y' == pomOriginal.properties.projectBuildDirectory.text()
    assert 'x${project.build.outputDirectory}y' == pomOriginal.properties.projectBuildOutputDirectory.text()
    assert 'x${project.build.sourceDirectory}y' == pomOriginal.properties.projectBuildSourceDirectory.text()
    assert 'x${project.basedir}y' == pomOriginal.properties.projectBasedir.text()
    assert 'x${project.baseUri}y' == pomOriginal.properties.projectBaseUri.text()
    assert 'x${project.build.scriptSourceDirectory}y' == pomOriginal.properties.projectBuildScriptSourceDirectory.text()
    assert 'x${project.build.testSourceDirectory}y' == pomOriginal.properties.projectBuildTestSourceDirectory.text()
    assert 'x${project.reporting.outputDirectory}y' == pomOriginal.properties.projectReportingOutputDirectory.text()
}


{
    final File flattenedPomFile = new File(basedir, '.flattened-pom.xml')
    assert flattenedPomFile.exists()

    final flattenedPom = new XmlSlurper().parse(flattenedPomFile)


    assert 'xissue-383-project-basedir-and-baseUri-for-are-not-interpolatedy' == flattenedPom.properties.projectArtifactId.text()
    assert 'xorg.codehaus.mojo.flatten.itsy' == flattenedPom.properties.projectGroupId.text()
    assert 'x0.0.1-SNAPSHOTy' == flattenedPom.properties.projectVersion.text()
    assert 'x${project.build.directory}y' == flattenedPom.properties.projectBuildDirectory.text()
    assert 'x${project.build.outputDirectory}y' == flattenedPom.properties.projectBuildOutputDirectory.text()
    assert 'x${project.build.sourceDirectory}y' == flattenedPom.properties.projectBuildSourceDirectory.text()
    assert 'x${project.basedir}y' == flattenedPom.properties.projectBasedir.text()
    assert 'x${project.baseUri}y' == flattenedPom.properties.projectBaseUri.text()
    assert 'x${project.build.scriptSourceDirectory}y' == flattenedPom.properties.projectBuildScriptSourceDirectory.text()
    assert 'x${project.build.testSourceDirectory}y' == flattenedPom.properties.projectBuildTestSourceDirectory.text()
    assert 'x${project.reporting.outputDirectory}y' == flattenedPom.properties.projectReportingOutputDirectory.text()
}