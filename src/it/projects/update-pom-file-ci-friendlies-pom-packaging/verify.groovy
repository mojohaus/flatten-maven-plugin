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

File flattenedPomFile = new File( basedir, '.flattened-pom.xml' )
assert flattenedPomFile.exists()
assert '0.0.1' == new XmlSlurper().parse( flattenedPomFile ).version.text()

// the flattened POM is installed instead of the original one, also for packaging pom
File installedPomFile = new File( localRepositoryPath, 'org/codehaus/mojo/flatten/its/update-pom-file-ci-friendlies-pom-packaging/0.0.1/update-pom-file-ci-friendlies-pom-packaging-0.0.1.pom' )
assert installedPomFile.exists()
assert installedPomFile.size() == flattenedPomFile.size()
assert '0.0.1' == new XmlSlurper().parse( installedPomFile ).version.text()
