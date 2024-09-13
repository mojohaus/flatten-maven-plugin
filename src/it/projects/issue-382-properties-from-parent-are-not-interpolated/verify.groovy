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
    final File bomOriginalFile = new File(basedir, 'bom/pom.xml')
    assert bomOriginalFile.exists()

    final bomOriginal = new XmlSlurper().parse(bomOriginalFile)
    assert 1 == bomOriginal.parent.size()
    assert 1 == bomOriginal.dependencyManagement.size()
    assert 1 == bomOriginal.dependencyManagement.dependencies.size()
    assert 2 == bomOriginal.dependencyManagement.dependencies.dependency.size()
    assert 'grpc-netty' == bomOriginal.dependencyManagement.dependencies.dependency[1].artifactId.text()
    assert '${version.grpc}' == bomOriginal.dependencyManagement.dependencies.dependency[1].version.text()
    assert 'spring-boot-dependencies' == bomOriginal.dependencyManagement.dependencies.dependency[0].artifactId.text()
    assert '${version.springboot}' == bomOriginal.dependencyManagement.dependencies.dependency[0].version.text()
    assert '${version.springboot}' == bomOriginal.properties.'interpolated-version-springboot'.text()
}
{
    final File flattenedBomPomFile = new File(basedir, 'bom/.flattened-pom.xml')
    assert flattenedBomPomFile.exists()

    final flattenedBom = new XmlSlurper().parse(flattenedBomPomFile)

    assert 1 == flattenedBom.dependencyManagement.size()
    assert 1 == flattenedBom.dependencyManagement.dependencies.size()
    assert 2 == flattenedBom.dependencyManagement.dependencies.dependency.size()
    assert 'grpc-netty' == flattenedBom.dependencyManagement.dependencies.dependency[1].artifactId.text()
    assert '1.57.1' == flattenedBom.dependencyManagement.dependencies.dependency[1].version.text()
    assert 'spring-boot-dependencies' == flattenedBom.dependencyManagement.dependencies.dependency[0].artifactId.text()
    assert '${version.springboot}' == flattenedBom.properties.'interpolated-version-springboot'.text()
    assert '${version.springboot}' == flattenedBom.dependencyManagement.dependencies.dependency[0].version.text()

}