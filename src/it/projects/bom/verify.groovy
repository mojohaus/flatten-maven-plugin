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
File originalPom = new File( basedir, 'bom/pom.xml' )
assert originalPom.exists()

def originalProject = new XmlSlurper().parse( originalPom )
// required and maven-plugin specific elements
assert '4.0.0' ==  originalProject.modelVersion.text()
assert 'org.codehaus.mojo.flatten.its' == originalProject.parent.groupId.text()
assert 'bom' == originalProject.artifactId.text()
assert '${bom.project.version}' == originalProject.version.text()
assert 'pom' == originalProject.packaging.text()
// banned elements for artifact
assert 1 == originalProject.parent.size()
assert 1 == originalProject.dependencyManagement.size()

File flattendPom = new File( basedir, 'bom/.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
// required elements
assert '4.0.0' ==  flattendProject.modelVersion.text()
assert 'org.codehaus.mojo.flatten.its' == flattendProject.groupId.text()
assert 'bom' == flattendProject.artifactId.text()
assert '0.9.1-beta1' == flattendProject.version.text()
assert 'pom' == originalProject.packaging.text()
// banned elements for artifact
assert 0 == flattendProject.build.size()
assert 0 == flattendProject.ciManagement.size()
assert 0 == flattendProject.contributors.size()
assert 0 == flattendProject.description.size()
assert 0 == flattendProject.developers.size()
assert 0 == flattendProject.distributionManagement.size()
assert 0 == flattendProject.issueManagement.size()
assert 0 == flattendProject.mailingLists.size()
assert 0 == flattendProject.modules.size()
assert 0 == flattendProject.name.size()
assert 0 == flattendProject.organization.size()
assert 0 == flattendProject.pluginRepositories.size()
assert 0 == flattendProject.prerequisites.size()
assert 0 == flattendProject.properties.size()
assert 0 == flattendProject.reporting.size()
assert 0 == flattendProject.reports.size()
assert 0 == flattendProject.scm.size()
assert 0 == flattendProject.url.size()

assert 0 == flattendProject.parent.size()
assert 1 == flattendProject.dependencyManagement.size()
assert 1 == flattendProject.dependencyManagement.dependencies.size()
// 259 = 11 - 1 + 249 (from expanded spring boot BOM)
assert 259 == flattendProject.dependencyManagement.dependencies.dependency.size()
assert 'spring-context' == flattendProject.dependencyManagement.dependencies.dependency[0].artifactId.text()
assert '4.0.8.RELEASE' == flattendProject.dependencyManagement.dependencies.dependency[0].version.text()

