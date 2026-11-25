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

// clean removes the flattened POM written in the validate phase; the validate phase of install writes it again
File flattenedPomFile = new File(basedir, 'target/.flattened-pom.xml')
assert flattenedPomFile.exists()

def flattenedPom = new XmlSlurper().parse(flattenedPomFile)
assert 0 == flattenedPom.build.size()
assert '1.1' == flattenedPom.dependencies.dependency[0].version.toString()

// Maven 4 installs the POM it builds itself, as flatten-deploy documents
if (mavenVersion.startsWith('3.')) {
    File installedPomFile = new File(localRepositoryPath, 'org/codehaus/mojo/flatten/its/clean-after-flatten/0.0.1/clean-after-flatten-0.0.1.pom')
    assert installedPomFile.exists()
    assert installedPomFile.text == flattenedPomFile.text
}
