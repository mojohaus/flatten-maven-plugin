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
assert 0 ==  originalProject.dependencies.size()
assert 0 == originalProject.profiles.size()

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
// expectation of PR #21 is actually wrong here
// TODO create a feature to get this expectation, activate in POM and uncomment asserts
// assert 1 ==  flattendProject.dependencies.size()
// assert 'dep' ==  flattendProject.dependencies.dependency.artifactId.text()
// assert '1.1' ==  flattendProject.dependencies.dependency.version.text()
assert 0 == flattendProject.profiles.profile.size()
