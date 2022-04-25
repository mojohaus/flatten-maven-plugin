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
assert 1 ==  originalProject.dependencies.size()
assert 'myprofile' == originalProject.profiles.profile.id.text()
assert 1 == originalProject.profiles.profile.dependencies.size()

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )

assert 1 ==  flattendProject.dependencies.size()
assert 2 ==  flattendProject.dependencies.dependency.size()

assert 'junit' ==  flattendProject.dependencies.dependency[0].artifactId.text()
assert '4.10' ==  flattendProject.dependencies.dependency[0].version.text()

assert 'dep' ==  flattendProject.dependencies.dependency[1].artifactId.text()
assert '1.1' ==  flattendProject.dependencies.dependency[1].version.text()

assert 0 == flattendProject.profiles.profile.size()
