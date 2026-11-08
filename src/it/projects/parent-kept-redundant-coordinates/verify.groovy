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

def sameGroup = new XmlSlurper().parse( new File( basedir, 'same-group/.flattened-pom.xml' ) )
assert 'org.codehaus.mojo.flatten.its' == sameGroup.parent.groupId.text()
assert '0.0.1-SNAPSHOT' == sameGroup.parent.version.text()
assert 'same-group' == sameGroup.artifactId.text()
assert 0 == sameGroup.groupId.size()
assert 0 == sameGroup.version.size()

def otherGroup = new XmlSlurper().parse( new File( basedir, 'other-group/.flattened-pom.xml' ) )
assert 'org.codehaus.mojo.flatten.its' == otherGroup.parent.groupId.text()
assert 'other-group' == otherGroup.artifactId.text()
assert 'org.codehaus.mojo.flatten.other' == otherGroup.groupId.text()
assert 0 == otherGroup.version.size()
