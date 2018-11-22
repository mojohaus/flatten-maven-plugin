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

// Check flattened POM of parent project
File flattenedParentPOM = new File( basedir, '.flattened-pom.xml' )
assert flattenedParentPOM.exists()

def flattenedParentProject = new XmlSlurper().parse( flattenedParentPOM )

// required elements
assert '4.0.0' ==  flattenedParentProject.modelVersion.text()
assert 'org.codehaus.mojo.flatten.its' == flattenedParentProject.groupId.text()
assert 'ci-multi-module-parent' == flattenedParentProject.artifactId.text()
assert 'pom' == flattenedParentProject.packaging.text()
assert '1.0-ci' == flattenedParentProject.version.text()
assert 'project 1.0-ci prod 1.0-ci' == flattenedParentProject.description.text()

// in this setup properties should be kept from original pom
assert '${ci.version}' == flattenedParentProject.properties.buildVersion.text()
assert 'test' == flattenedParentProject.properties.buildMode.text()

// Check flattened POM of child project

File flattenedChildPOM = new File( basedir, 'ci-child/.flattened-pom.xml' )
assert flattenedChildPOM.exists()

def flattenedChildProject = new XmlSlurper().parse( flattenedChildPOM )
assert '4.0.0' ==  flattenedChildProject.modelVersion.text()

assert 'org.codehaus.mojo.flatten.its' == flattenedChildProject.parent.groupId.text()
assert '1.0-ci' == flattenedChildProject.parent.version.text()

assert 'ci-multi-module-child' == flattenedChildProject.artifactId.text()
assert '1.0-ci' == flattenedChildProject.version.text()
assert 'pom' == flattenedChildProject.packaging.text()
