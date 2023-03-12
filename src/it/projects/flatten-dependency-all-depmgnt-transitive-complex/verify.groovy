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

File originalPom = new File( basedir, 'pom.xml' )
assert originalPom.exists()

def originalProject = new XmlSlurper().parse( originalPom )
assert 0 == originalProject.dependencies.size()
assert 1 == originalProject.dependencyManagement.size()

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
assert 0 == flattendProject.dependencies.size()
assert 1 == flattendProject.dependencyManagement.size()

def flattendModuleAPom = new File( basedir , 'module-a/.flattened-pom.xml' )
assert flattendModuleAPom.exists()

def flattendModuleAProject = new XmlSlurper().parse( flattendModuleAPom )
assert 1 == flattendModuleAProject.dependencies.size()
assert 2 == flattendModuleAProject.dependencies.dependency.size()

def core = flattendModuleAProject.dependencies.dependency.find {
    it.groupId == 'org.codehaus.mojo.flatten.its' && it.artifactId == 'core'
}
assert '3.2.1' == core.version.text()
assert 1 == core.exclusions.size()
assert 1 == core.exclusions.exclusion.size()
assert null != core.exclusions.exclusion.find { it.groupId == 'org.codehaus.mojo.flatten.its' && it.artifactId == 'dep' }

def dep = flattendModuleAProject.dependencies.dependency.find {
    it.groupId == 'org.codehaus.mojo.flatten.its' && it.artifactId == 'dep'
}
assert null != dep
assert '1.1' == dep.version.text()

def flattendModuleBPom = new File( basedir, 'module-b/.flattened-pom.xml' )
assert flattendModuleBPom.exists()

def flattendModuleBProject = new XmlSlurper().parse( flattendModuleBPom )
assert 1 == flattendModuleBProject.dependencies.size()
assert 1 == flattendModuleBProject.dependencies.dependency.size()
