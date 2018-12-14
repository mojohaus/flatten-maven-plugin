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

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

File originalPom = new File( basedir, 'pom.xml' )
assert originalPom.exists()
 
def originalProject = new XmlSlurper().parse( originalPom )
assert '${revision}' == originalProject.dependencies.dependency[0].version.text()
assert '${small}' == originalProject.dependencies.dependency[0].classifier.text()

File flattenedPom = new File( basedir, '.flattened-pom.xml' )
assert flattenedPom.exists()
def flattenedProject = new XmlSlurper().parse( flattenedPom )


assert '1.2.3.4' == flattenedProject.version.text()
assert '1.2.3.4' == flattenedProject.dependencies.dependency[0].version.text()
assert '${small}' == flattenedProject.dependencies.dependency[0].classifier.text()

assert '1.2.3.1' == flattenedProject.dependencies.dependency[1].version.text()
assert '${small1}' == flattenedProject.dependencies.dependency[1].classifier.text()

assert 'revision' == flattenedProject.dependencies.dependency[2].version.text()
assert 'small2' == flattenedProject.dependencies.dependency[2].classifier.text()

assert '1.2.3.4' == flattenedProject.profiles.profile.properties.profilerevision.text()
assert '${revision1}' == flattenedProject.profiles.profile.properties.profilerevision1.text()
