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
assert 'file.txt' == originalProject.profiles.profile[0].activation.file.exists.text()
assert '${basedir}/file.txt' == originalProject.profiles.profile[1].activation.file.exists.text()
assert '${any.property}' == originalProject.profiles.profile[2].activation.file.exists.text()
assert '${revision}' == originalProject.profiles.profile[3].activation.file.exists.text()

File flattenedPom = new File( basedir, '.flattened-pom.xml' )
assert flattenedPom.exists()
def flattenedProject = new XmlSlurper().parse( flattenedPom )

assert 'file.txt' == flattenedProject.profiles.profile[0].activation.file.exists.text()
assert '${basedir}/file.txt' == flattenedProject.profiles.profile[1].activation.file.exists.text()
assert '${any.property}' == flattenedProject.profiles.profile[2].activation.file.exists.text()
assert '1.2.3.4' == flattenedProject.profiles.profile[3].activation.file.exists.text()
