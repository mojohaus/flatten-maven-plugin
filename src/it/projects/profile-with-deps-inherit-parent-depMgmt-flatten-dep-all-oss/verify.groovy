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
File originalPom = new File( basedir, 'pom.xml' )
assert originalPom.exists()

def originalProject = new XmlSlurper().parse( originalPom )
// required elements
assert '4.0.0' ==  originalProject.modelVersion.text()
assert 'org.codehaus.mojo.flatten.its' == originalProject.groupId.text()
assert 'profile-with-deps-inherit-parent-depMgmt-flatten-dep-all-oss' == originalProject.artifactId.text()
assert '0.1-SNAPSHOT' == originalProject.version.text()
// banned elements for artifact
assert 1 == originalProject.name.size()
assert 1 == originalProject.description.size()
assert 1 == originalProject.url.size()
assert 0 == originalProject.developers.size()
assert 0 == originalProject.organization.size()
assert 0 == originalProject.issueManagement.size()
assert 0 == originalProject.scm.size()
assert 0 == originalProject.distributionManagement.size()
assert 0 == originalProject.properties.size()
assert 0 == originalProject.pluginRepositories.size()
assert 1 == originalProject.build.size()
assert 1 == originalProject.parent.size()
assert 0 == originalProject.dependencyManagement.size()
assert 0 == originalProject.reporting.size()
assert 1 == originalProject.profiles.size()

File bom = new File( basedir, 'bom/pom.xml' )
assert bom.exists()

def bomProject = new XmlSlurper().parse( bom )
// required elements
assert '4.0.0' ==  bomProject.modelVersion.text()
assert 'org.codehaus.mojo.flatten.its' == bomProject.groupId.text()
assert 'profile-with-deps-inherit-parent-depMgmt-flatten-dep-all-oss-bom' == bomProject.artifactId.text()
assert '0.1-SNAPSHOT' == bomProject.version.text()

assert 0 == bomProject.name.size()
assert 0 == bomProject.description.size()
assert 0 == bomProject.url.size()
assert 1 == bomProject.developers.size()
assert 1 == bomProject.organization.size()
assert 1 == bomProject.issueManagement.size()
assert 1 == bomProject.scm.size()
assert 1 == bomProject.distributionManagement.size()
assert 1 == bomProject.properties.size()
assert 1 == bomProject.pluginRepositories.size()
assert 0 == bomProject.build.size()
assert 0 == bomProject.parent.size()
assert 1 == bomProject.dependencyManagement.size()
assert 1 == bomProject.reporting.size()
assert 0 == bomProject.profiles.size()

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
// required elements
assert '4.0.0' ==  flattendProject.modelVersion.text()
assert 'org.codehaus.mojo.flatten.its' == flattendProject.groupId.text()
assert 'profile-with-deps-inherit-parent-depMgmt-flatten-dep-all-oss' == flattendProject.artifactId.text()
assert '0.1-SNAPSHOT' == flattendProject.version.text()
// banned elements for artifact
assert 1 == flattendProject.name.size()
assert 1 == flattendProject.description.size()
assert 1 == flattendProject.url.size()
assert 1 == flattendProject.developers.size()
assert 1 == flattendProject.organization.size()
assert 1 == flattendProject.issueManagement.size()
assert 1 == flattendProject.scm.size()
assert 1 == flattendProject.distributionManagement.size()
assert 0 == flattendProject.properties.size()
assert 0 == flattendProject.pluginRepositories.size()
assert 0 == flattendProject.build.size()
assert 0 == flattendProject.parent.size()
assert 0 == flattendProject.dependencyManagement.size()
assert 0 == flattendProject.reporting.size()

assert 1 == flattendProject.profiles.size()
assert 1 == flattendProject.profiles.profile.size()
assert 'java9' == flattendProject.profiles.profile.id.text()
assert 1 == flattendProject.profiles.profile.dependencies.size()
assert 1 == flattendProject.profiles.profile.dependencies.dependency.size()
assert 'javax.annotation-api' == flattendProject.profiles.profile.dependencies.dependency.artifactId.text()
assert '1.3.2' == flattendProject.profiles.profile.dependencies.dependency.version.text()
