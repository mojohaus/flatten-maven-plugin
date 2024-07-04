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

{
    final File parentOriginalFile = new File(basedir, 'pom.xml')
    assert parentOriginalFile.exists()

    final parentOriginal = new XmlSlurper().parse(parentOriginalFile)
    assert '${project.version}' == parentOriginal.dependencyManagement.dependencies.dependency[0].version.text()

}

{
    final File flattenedParentPomFile = new File(basedir, '.flattened-pom.xml')
    assert flattenedParentPomFile.exists()

    final flattenedParent = new XmlSlurper().parse(flattenedParentPomFile)
    assert 0 == flattenedParent.parent.size()
    assert '0.0.1-SNAPSHOT' == flattenedParent.properties.'interpolated-project-version'.text()
    assert '0.0.1-SNAPSHOT' == flattenedParent.dependencyManagement.dependencies.dependency[0].version.text()
}


{
    final File bomOriginalFile = new File(basedir, 'bom/pom.xml')
    assert bomOriginalFile.exists()

    final bomOriginal = new XmlSlurper().parse(bomOriginalFile)
    assert '0.0.1-SNAPSHOT' == bomOriginal.parent.version.text()
    assert 1 == bomOriginal.parent.size()
    assert 1 == bomOriginal.dependencyManagement.size()
    assert 1 == bomOriginal.dependencyManagement.dependencies.size()
    assert 1 == bomOriginal.dependencyManagement.dependencies.dependency.size()
    assert '${project.version}' == bomOriginal.properties.'interpolated-project-version'.text()
    assert '${project.version}' == bomOriginal.dependencyManagement.dependencies.dependency[0].version.text()
}

{
    final File flattenedBomPomFile = new File(basedir, 'bom/.flattened-pom.xml')
    assert flattenedBomPomFile.exists()

    final flattenedBom = new XmlSlurper().parse(flattenedBomPomFile)
    assert 0 == flattenedBom.parent.size()
    assert '${project.version}' == flattenedBom.properties.'interpolated-project-version'.text()
    assert '${project.version}' == flattenedBom.dependencyManagement.dependencies.dependency[0].version.text()
}

