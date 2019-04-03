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
assert 1 ==  originalProject.dependencies.size()

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
assert 1 ==  flattendProject.dependencies.size()
assert 2 ==  flattendProject.dependencies.dependency.size()

def core = flattendProject.dependencies.dependency.find {
    it.groupId == 'org.codehaus.mojo.flatten.its' && it.artifactId == 'core'
}
assert '3.2.1' == core.version.text()
assert 1 == core.exclusions.size()
assert 1 == core.exclusions.exclusion.size()
assert null != core.exclusions.exclusion.find { it.groupId == 'org.codehaus.mojo.flatten.its' && it.artifactId == 'dep' }

def dep = flattendProject.dependencies.dependency.find {
    it.groupId == 'org.codehaus.mojo.flatten.its' && it.artifactId == 'dep'
}
assert null != dep
assert '3.2.1' == dep.version.text()


