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

File originalPom = new File( basedir, 'pom.xml' )
assert originalPom.exists()

def originalProject = new XmlSlurper().parse( originalPom )
// required elements
assert '4.0.0' ==  originalProject.modelVersion.text()
assert 'org.codehaus.mojo.flatten.its' == originalProject.groupId.text()
assert 'external-parent' == originalProject.artifactId.text()
assert '0.0.1-SNAPSHOT' == originalProject.version.text()
// banned elements for artifact
assert 1 == originalProject.ciManagement.size()
assert 1 == originalProject.contributors.size()
assert 1 == originalProject.dependencies.size()
assert 1 == originalProject.dependencyManagement.size()
assert 1 == originalProject.description.size()
assert 1 == originalProject.developers.size()
assert 1 == originalProject.distributionManagement.size()
assert 1 == originalProject.issueManagement.size()
assert 1 == originalProject.mailingLists.size()
assert 0 == originalProject.name.size()
assert 1 == originalProject.organization.size()
assert 1 == originalProject.parent.size()
assert 1 == originalProject.pluginRepositories.size()
assert 1 == originalProject.repositories.size()
assert 1 == originalProject.prerequisites.size()
assert 1 == originalProject.properties.size()
assert 1 == originalProject.reporting.size()
assert 1 == originalProject.scm.size()
assert 1 == originalProject.url.size()

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
// required elements
assert '4.0.0' ==  flattendProject.modelVersion.text()
assert 'org.codehaus.mojo.flatten.its' == flattendProject.groupId.text()
assert 'external-parent' == flattendProject.artifactId.text()
assert '0.0.1-SNAPSHOT' == flattendProject.version.text()
// banned elements for artifact
assert 0 == flattendProject.build.size()
assert 0 == flattendProject.ciManagement.size()
assert 0 == flattendProject.contributors.size()
assert 0 == flattendProject.dependencyManagement.size()
assert 0 == flattendProject.description.size()
assert 0 == flattendProject.developers.size()
assert 0 == flattendProject.distributionManagement.size()
assert 0 == flattendProject.issueManagement.size()
assert 0 == flattendProject.mailingLists.size()
assert 0 == flattendProject.name.size()
assert 0 == flattendProject.organization.size()
assert 0 == flattendProject.parent.size()
assert 0 == flattendProject.pluginRepositories.size()
assert 1 == flattendProject.repositories.size()
assert 1 == flattendProject.repositories.repository.size()
assert 'no-one' == flattendProject.repositories.repository[0].id.text()
assert 0 == flattendProject.prerequisites.size()
assert 0 == flattendProject.properties.size()
assert 0 == flattendProject.reporting.size()
assert 0 == flattendProject.reports.size()
assert 0 == flattendProject.scm.size()
assert 0 == flattendProject.url.size()
assert 1 == flattendProject.dependencies.size()
assert 1 == flattendProject.dependencies.dependency.size()
assert '4.0.5.RELEASE' == flattendProject.dependencies.dependency[0].version.text()

assert 1 == flattendProject.profiles.size()
assert 1 == flattendProject.profiles.profile.size()
assert 'test' == flattendProject.profiles.profile.id.text()
assert 1 == flattendProject.profiles.profile.dependencies.size()
assert 1 == flattendProject.profiles.profile.dependencies.dependency.size()
assert '1.1.0.RELEASE' == flattendProject.profiles.profile.dependencies.dependency.version.text()
