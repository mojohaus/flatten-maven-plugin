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
// required and maven-plugin specific elements
assert '4.0.0' ==  originalProject.modelVersion.text()
assert 'org.codehaus.mojo.flatten.its' == originalProject.groupId.text()
assert 'complete-multimodule-parent-pom-cifriendly' == originalProject.artifactId.text()
assert '${revision}' == originalProject.version.text()
assert 'pom' == originalProject.packaging.text()
// banned elements for artifact
assert 1 == originalProject.build.size()
assert 1 == originalProject.ciManagement.size()
assert 1 == originalProject.contributors.size()
assert 1 == originalProject.dependencyManagement.size()
assert 1 == originalProject.description.size()
assert 1 == originalProject.developers.size()
assert 1 == originalProject.distributionManagement.size()
assert 1 == originalProject.issueManagement.size()
assert 1 == originalProject.mailingLists.size()
assert 1 == originalProject.modules.size()
assert 1 == originalProject.name.size()
assert 1 == originalProject.organization.size()
assert 1 == originalProject.parent.size()
assert 1 == originalProject.pluginRepositories.size()
assert 1 == originalProject.prerequisites.size()
assert 1 == originalProject.properties.size()
assert 1 == originalProject.reporting.size()
assert 1 == originalProject.reports.size()
assert 1 == originalProject.scm.size()
assert 1 == originalProject.url.size()

// Parent module
File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
// required elements
assert '4.0.0' ==  flattendProject.modelVersion.text()
assert 'org.codehaus.mojo.flatten.its' == flattendProject.groupId.text()
assert 'complete-multimodule-parent-pom-cifriendly' == flattendProject.artifactId.text()
assert '1.2.3.4' == flattendProject.version.text()
assert 'pom' == originalProject.packaging.text()

// Child module: module
File flattendChildPom = new File( basedir, 'module/.flattened-pom.xml' )
assert flattendChildPom.exists()

def flattendChildProject = new XmlSlurper().parse( flattendChildPom )
// required elements
assert '4.0.0' ==  flattendChildProject.modelVersion.text()
assert 'org.codehaus.mojo.flatten.its' == flattendChildProject.groupId.text()
assert 'multimodule-module-cifriendly' == flattendChildProject.artifactId.text()
assert '1.2.3.4' == flattendChildProject.version.text()

// Child module: module-with-parent
File flattendChildWithParentPom = new File( basedir, 'module-with-parent/.flattened-pom.xml' )
assert flattendChildWithParentPom.exists()

def flattendChildWithParentProject = new XmlSlurper().parse( flattendChildWithParentPom )
// required elements
assert '4.0.0' ==  flattendChildWithParentProject.modelVersion.text()
assert 'org.codehaus.mojo.flatten.its' == flattendChildWithParentProject.groupId.text()
assert 'multimodule-module-with-parent-cifriendly' == flattendChildWithParentProject.artifactId.text()
assert '1.2.3.4' == flattendChildWithParentProject.version.text()
assert '1.2.3.4' == flattendChildWithParentProject.parent.version.text()