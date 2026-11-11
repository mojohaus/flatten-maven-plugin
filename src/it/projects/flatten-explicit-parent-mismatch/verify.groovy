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

def project = new XmlSlurper().parse( new File( basedir, 'explicit-child/.flattened-pom.xml' ) )
assert 'parent-depMngt' == project.parent.artifactId.text()
assert '2' == project.parent.version.text()
assert 1 == project.parent.relativePath.size()
assert '' == project.parent.relativePath.text()

def buildLog = new File( basedir, 'build.log' ).text
def mismatchWarnings = buildLog.readLines().findAll { it.contains( '[WARNING] The POM at ' ) }
assert mismatchWarnings.size() == 1
assert mismatchWarnings[0].contains( 'explicit-child' )
assert mismatchWarnings[0].contains( 'org.codehaus.mojo.flatten.its:unrelated-aggregator:1' )
assert mismatchWarnings[0].contains( 'expected parent org.codehaus.mojo.flatten.its:parent-depMngt:2' )
