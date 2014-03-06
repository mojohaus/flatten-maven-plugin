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
assert 'org.codehaus.mojo.consumer.its' == originalProject.groupId.text()
assert 'complete-multimodule-parent-pom' == originalProject.artifactId.text()
assert '0.0.1-SNAPSHOT' == originalProject.version.text()
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

File consumerPom = new File( basedir, 'target/consumer-pom.xml' )
assert consumerPom.exists()

def consumerProject = new XmlSlurper().parse( consumerPom )
// required elements
assert '4.0.0' ==  consumerProject.modelVersion.text()
assert 'org.codehaus.mojo.consumer.its' == consumerProject.groupId.text()
assert 'complete-multimodule-parent-pom' == consumerProject.artifactId.text()
assert '0.0.1-SNAPSHOT' == consumerProject.version.text()
assert 'pom' == originalProject.packaging.text()
// banned elements for artifact
assert 0 == consumerProject.build.size()
assert 0 == consumerProject.ciManagement.size()
assert 0 == consumerProject.contributors.size()
assert 0 == consumerProject.dependencyManagement.size()
assert 0 == consumerProject.description.size()
assert 0 == consumerProject.developers.size()
assert 0 == consumerProject.distributionManagement.size()
assert 0 == consumerProject.issueManagement.size()
assert 0 == consumerProject.mailingLists.size()
assert 0 == consumerProject.modules.size()
assert 0 == consumerProject.name.size()
assert 0 == consumerProject.organization.size()
assert 0 == consumerProject.parent.size()
assert 0 == consumerProject.pluginRepositories.size()
assert 0 == consumerProject.prerequisites.size()
assert 0 == consumerProject.properties.size()
assert 0 == consumerProject.reporting.size()
assert 0 == consumerProject.reports.size()
assert 0 == consumerProject.scm.size()
assert 0 == consumerProject.url.size()

