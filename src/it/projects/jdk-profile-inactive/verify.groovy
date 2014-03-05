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

File consumerPom = new File( basedir, 'target/consumer-pom.xml' )
assert consumerPom.exists()

def consumerProject = new XmlSlurper().parse( consumerPom )
// profile elements to include
assert 0 ==  consumerProject.dependencies.size()
assert 'jdk' == consumerProject.profiles.profile.id.text()
assert 1 == consumerProject.profiles.profile.activation.size()
assert 1 == consumerProject.profiles.profile.dependencies.size()
assert 1 == consumerProject.profiles.profile.repositories.size()
// profile elements to exclude
assert 0 == consumerProject.profiles.profile.build.size()
assert 0 == consumerProject.profiles.profile.dependencyManagement.size()
assert 0 == consumerProject.profiles.profile.distributionManagement.size()
assert 0 == consumerProject.profiles.profile.modules.size()
assert 0 == consumerProject.profiles.profile.pluginRepositories.size()
assert 0 == consumerProject.profiles.profile.properties.size()
assert 0 == consumerProject.profiles.profile.reporting.size()
assert 0 == consumerProject.profiles.profile.reports.size()

