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
assert '${timestamp}-GID${revision}' == originalProject.version.text()
assert '${maven.build.timestamp}' == originalProject.properties.timestamp.text()

File flattenedPom = new File( basedir, '.flattened-pom.xml' )
assert flattenedPom.exists()
def flattenedProject = new XmlSlurper().parse( flattenedPom )

// The version should have both ${timestamp} and ${revision} resolved
def version = flattenedProject.version.text()
assert !version.contains('${timestamp}'), "Version should not contain unresolved \${timestamp}: ${version}"
assert !version.contains('${revision}'), "Version should not contain unresolved \${revision}: ${version}"
assert version.contains('-GID'), "Version should contain '-GID' separator: ${version}"
assert version.contains('developer'), "Version should contain 'developer' from revision: ${version}"
// Check that it matches the expected format (timestamp is yyyyMMdd-HHmmss)
assert version ==~ /\d{8}-\d{6}-GIDdeveloper/, "Version should match pattern yyyyMMdd-HHmmss-GIDdeveloper: ${version}"

// Also check dependency version is resolved
def depVersion = flattenedProject.dependencies.dependency[0].version.text()
assert !depVersion.contains('${timestamp}'), "Dependency version should not contain unresolved \${timestamp}: ${depVersion}"
assert !depVersion.contains('${revision}'), "Dependency version should not contain unresolved \${revision}: ${depVersion}"
assert depVersion.contains('-GID'), "Dependency version should contain '-GID' separator: ${depVersion}"
assert depVersion.contains('developer'), "Dependency version should contain 'developer': ${depVersion}"
assert depVersion ==~ /\d{8}-\d{6}-GIDdeveloper/, "Dependency version should match pattern: ${depVersion}"
