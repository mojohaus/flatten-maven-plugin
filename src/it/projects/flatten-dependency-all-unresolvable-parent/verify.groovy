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
assert 1 ==  originalProject.dependencies.dependency.size()

File flattendPom = new File( basedir, '.flattened-pom.xml' )
assert flattendPom.exists()

def flattendProject = new XmlSlurper().parse( flattendPom )

// The flattened POM should have resolved all transitive dependencies
// Even if some dependency parents cannot be resolved, the plugin should not fail
assert flattendProject.dependencies.dependency.size() > 0

// Check that querydsl-core is included
def querydslCore = flattendProject.dependencies.dependency.find { 
    it.groupId.text() == 'com.querydsl' && it.artifactId.text() == 'querydsl-core'
}
assert querydslCore != null
assert querydslCore.version.text() == '4.3.1'

println "Test passed: Flattened POM created successfully even with unresolvable parent POMs"
