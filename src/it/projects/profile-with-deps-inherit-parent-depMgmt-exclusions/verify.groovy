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

File flattenedPom = new File( basedir, '.flattened-pom.xml' )
assert flattenedPom.exists()

def flattenedProject = new XmlSlurper().parse( flattenedPom )
def dependencies = flattenedProject.profiles.profile.dependencies.dependency
assert 2 == dependencies.size()

// no exclusions declared in the profile: the managed ones are taken
def dep = dependencies.find { it.artifactId.text() == 'dep' }
assert '1.1' == dep.version.text()
assert ['excluded-by-management'] == dep.exclusions.exclusion.artifactId.collect { it.text() }

// exclusions declared in the profile win over the managed ones
def dep1 = dependencies.find { it.artifactId.text() == 'dep1' }
assert '2.2' == dep1.version.text()
assert ['excluded-by-profile'] == dep1.exclusions.exclusion.artifactId.collect { it.text() }
