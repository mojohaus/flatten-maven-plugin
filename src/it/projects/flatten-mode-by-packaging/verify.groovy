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

// pom packaging resolves to resolveCiFriendliesOnly: the parent keeps its build for its children.
def parent = new XmlSlurper().parse( new File( basedir, '.flattened-pom.xml' ) )
assert '1.2.3.4' == parent.version.text()
assert 1 == parent.build.size()
assert 1 == parent.properties.size()

// jar packaging resolves to defaults: a minimal consumer POM without parent or build.
def module = new XmlSlurper().parse( new File( basedir, 'module/.flattened-pom.xml' ) )
assert '1.2.3.4' == module.version.text()
assert 0 == module.parent.size()
assert 0 == module.build.size()
