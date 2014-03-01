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
assert '${depGroupId}' == originalProject.dependencies.dependency.groupId.text()
assert '${depArtifactId}' == originalProject.dependencies.dependency.artifactId.text()
assert '${depVersion}' == originalProject.dependencies.dependency.version.text()
assert '${utilGroupId}' == originalProject.profiles.profile.dependencies.dependency.groupId.text()
assert '${utilArtifactId}' == originalProject.profiles.profile.dependencies.dependency.artifactId.text()
assert '${utilVersion}' == originalProject.profiles.profile.dependencies.dependency.version.text()

File consumerPom = new File( basedir, 'target/consumer-pom.xml' )
assert consumerPom.exists()

def consumerProject = new XmlSlurper().parse( consumerPom )
assert 'org.codehaus.mojo.consumer.its' == consumerProject.dependencies.dependency.groupId.text()
assert 'dep' == consumerProject.dependencies.dependency.artifactId.text()
assert '1.1' == consumerProject.dependencies.dependency.version.text()
assert 'org.codehaus.mojo.consumer.its' == consumerProject.profiles.profile.dependencies.dependency.groupId.text()
assert 'util' == consumerProject.profiles.profile.dependencies.dependency.artifactId.text()
assert '3.2.1' == consumerProject.profiles.profile.dependencies.dependency.version.text()
