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
assert 2 ==  originalProject.dependencies.dependency.size()
assert "dep" ==  originalProject.dependencies.dependency[1].artifactId.text()
assert "3.2.1" ==  originalProject.dependencies.dependency[1].version.text()
assert "test" ==  originalProject.dependencies.dependency[1].scope.text()

File flattenedPom = new File( basedir, '.flattened-pom.xml' )
assert flattenedPom.exists()

def flattenedProject = new XmlSlurper().parse( flattenedPom )

// core and dep should be there. It's because while the test-scope dep (the
// direct dependency), core declares dep as compile-scope (default) dependency.
assert 2 ==  flattenedProject.dependencies.dependency.size()

assert "core" ==  flattenedProject.dependencies.dependency[0].artifactId.text()
assert "3.2.1" ==  flattenedProject.dependencies.dependency[0].version.text()
assert "compile" ==  flattenedProject.dependencies.dependency[0].scope.text()

// The flattened pom.xml should declare the dep under core as compile scope.
// It's ok to ignore the one in the test-scope dependency.
assert "dep" ==  flattenedProject.dependencies.dependency[1].artifactId.text()
assert "3.2.1" ==  flattenedProject.dependencies.dependency[1].version.text()
assert "compile" ==  flattenedProject.dependencies.dependency[1].scope.text()
