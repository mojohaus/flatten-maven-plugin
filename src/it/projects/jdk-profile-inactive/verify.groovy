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
// profile elements to include
assert 0 ==  originalProject.dependencies.size()
assert 'jdk' == originalProject.profiles.profile.id.text()
assert 1 == originalProject.profiles.profile.activation.size()
assert 1 == originalProject.profiles.profile.dependencies.size()
assert 1 == originalProject.profiles.profile.repositories.size()
// profile elements to exclude
assert 1 == originalProject.profiles.profile.build.size()
assert 1 == originalProject.profiles.profile.dependencyManagement.size()
assert 1 == originalProject.profiles.profile.distributionManagement.size()
assert 1 == originalProject.profiles.profile.modules.size()
assert 1 == originalProject.profiles.profile.pluginRepositories.size()
assert 1 == originalProject.profiles.profile.properties.size()
assert 1 == originalProject.profiles.profile.reporting.size()
assert 1 == originalProject.profiles.profile.reports.size()

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )
// profile elements to include
assert 0 ==  flattendProject.dependencies.size()
assert 'jdk' == flattendProject.profiles.profile.id.text()
assert 1 == flattendProject.profiles.profile.activation.size()
assert 1 == flattendProject.profiles.profile.dependencies.size()
assert 1 == flattendProject.profiles.profile.repositories.size()
// profile elements to exclude
assert 0 == flattendProject.profiles.profile.build.size()
assert 0 == flattendProject.profiles.profile.dependencyManagement.size()
assert 0 == flattendProject.profiles.profile.distributionManagement.size()
assert 0 == flattendProject.profiles.profile.modules.size()
assert 0 == flattendProject.profiles.profile.pluginRepositories.size()
assert 0 == flattendProject.profiles.profile.properties.size()
assert 0 == flattendProject.profiles.profile.reporting.size()
assert 0 == flattendProject.profiles.profile.reports.size()

