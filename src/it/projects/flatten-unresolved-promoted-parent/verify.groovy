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

def root = new XmlSlurper().parse( new File( basedir, '.flattened-pom.xml' ) )
assert 'parent-depMngt' == root.parent.artifactId.text()
assert '2' == root.parent.version.text()
assert !new File( basedir, 'child/.flattened-pom.xml' ).exists()

def log = new File( basedir, 'build.log' ).text
assert log.contains( 'Unresolved published parent version for org.codehaus.mojo.flatten.its:parent-depMngt: ${it.parent.version}' )
