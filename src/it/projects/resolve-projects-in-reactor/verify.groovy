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

File originalPom = new File( basedir, 'jar/pom.xml' )
assert originalPom.exists()
 
def originalProject = new XmlSlurper().parse( originalPom )
assert 'junit' == originalProject.dependencies.dependency.groupId.text()
assert 'junit' == originalProject.dependencies.dependency.artifactId.text()
assert '' == originalProject.dependencies.dependency.version.text()

File flattenedPom = new File( basedir, 'jar/.flattened-pom.xml' )
assert flattenedPom.exists()

def flattenedProject = new XmlSlurper().parse( flattenedPom )
assert 'junit' == flattenedProject.dependencies.dependency.groupId.text()
assert 'junit' == flattenedProject.dependencies.dependency.artifactId.text()
assert '4.13.2' == flattenedProject.dependencies.dependency.version.text()
