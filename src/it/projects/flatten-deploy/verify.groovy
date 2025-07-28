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

File flattenedPomFile = new File(basedir, '.flattened-pom.xml')
assert flattenedPomFile.exists()

def flattenedPom = new XmlSlurper().parse(flattenedPomFile)
// check that the pom is flattened
assert 0 == flattenedPom.build.pluginManagement.size()
assert 0 == flattenedPom.build.plugins.size()
assert "1.1" == flattenedPom.dependencies.dependency[0].version.toString()

if (mavenVersion.startsWith('3.')) {

    // check installed pom
    File flattenedInstallPomFile = new File(localRepositoryPath, 'org/codehaus/mojo/flatten/its/flatten-deploy/0.0.1/flatten-deploy-0.0.1.pom')
    assert flattenedInstallPomFile.exists()
    assert flattenedInstallPomFile.size() == flattenedPomFile.size()

    def flattenedInstallPom = new XmlSlurper().parse(flattenedInstallPomFile)
    assert flattenedInstallPom == flattenedPom

    // check deployed pom
    File flattenedDeployPomFile = new File(basedir, 'target/repo/org/codehaus/mojo/flatten/its/flatten-deploy/0.0.1/flatten-deploy-0.0.1.pom')
    assert flattenedDeployPomFile.exists()
    assert flattenedDeployPomFile.size() == flattenedPomFile.size()

    def flattenedDeployPom = new XmlSlurper().parse(flattenedDeployPomFile)
    assert flattenedDeployPom == flattenedPom
} else {
    // Maven 4.x
    // check installed build pom
    File flattenedInstallPomFile = new File(localRepositoryPath, 'org/codehaus/mojo/flatten/its/flatten-deploy/0.0.1/flatten-deploy-0.0.1-build.pom')
    assert flattenedInstallPomFile.exists()
    assert flattenedInstallPomFile.size() == flattenedPomFile.size()

    def flattenedInstallPom = new XmlSlurper().parse(flattenedInstallPomFile)
    assert flattenedInstallPom == flattenedPom

    // check deployed consumer pom
    flattenedInstallPomFile = new File(localRepositoryPath, 'org/codehaus/mojo/flatten/its/flatten-deploy/0.0.1/flatten-deploy-0.0.1.pom')
    flattenedInstallPom = new XmlSlurper().parse(flattenedInstallPomFile)
    assert flattenedInstallPom == flattenedPom

    // check deployed build pom
    File flattenedDeployPomFile = new File(basedir, 'target/repo/org/codehaus/mojo/flatten/its/flatten-deploy/0.0.1/flatten-deploy-0.0.1-build.pom')
    assert flattenedDeployPomFile.exists()
    assert flattenedDeployPomFile.size() == flattenedPomFile.size()

    def flattenedDeployPom = new XmlSlurper().parse(flattenedDeployPomFile)
    assert flattenedDeployPom == flattenedPom

    flattenedDeployPomFile = new File(basedir, 'target/repo/org/codehaus/mojo/flatten/its/flatten-deploy/0.0.1/flatten-deploy-0.0.1.pom')
    flattenedDeployPom = new XmlSlurper().parse(flattenedDeployPomFile)
    assert flattenedDeployPom == flattenedPom
}
