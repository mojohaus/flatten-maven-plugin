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

File repositoryPom = new File( basedir, '.flattened-pom.xml' )
File modulePom = new File( basedir, 'module-parent/.flattened-pom.xml' )
File childPom = new File( basedir, 'child/.flattened-pom.xml' )
File directoryParentChildPom = new File( basedir, 'directory-parent-child/.flattened-pom.xml' )
assert repositoryPom.exists()
assert modulePom.exists()
assert childPom.exists()
assert directoryParentChildPom.exists()

def repositoryProject = new XmlSlurper().parse( repositoryPom )
def moduleProject = new XmlSlurper().parse( modulePom )
def childProject = new XmlSlurper().parse( childPom )
def directoryParentChild = new XmlSlurper().parse( directoryParentChildPom )

assert 'parent-depMngt' == repositoryProject.parent.artifactId.text()
assert 'parent-depMngt' == moduleProject.parent.artifactId.text()
assert 'parent-depMngt' == childProject.parent.artifactId.text()
assert 'parent-depMngt' == directoryParentChild.parent.artifactId.text()
assert 'module-value' == directoryParentChild.properties.'module.property'.text()
assert 1 == repositoryProject.parent.relativePath.size()
assert '' == repositoryProject.parent.relativePath.text()
assert 1 == moduleProject.parent.relativePath.size()
assert '' == moduleProject.parent.relativePath.text()
assert 1 == childProject.parent.relativePath.size()
assert '' == childProject.parent.relativePath.text()
assert 1 == directoryParentChild.parent.relativePath.size()
assert '' == directoryParentChild.parent.relativePath.text()
// The groupId equals the kept parent's, so it is omitted rather than restated.
assert 0 == childProject.groupId.size()
assert 'org.codehaus.mojo.flatten.its' == childProject.parent.groupId.text()
assert '1.2.3.4' == childProject.version.text()
assert 'repository-value' == childProject.properties.'repository.property'.text()
assert 'module-value' == childProject.properties.'module.property'.text()
assert 'profile-value' == childProject.properties.'active.parent.property'.text()
assert !repositoryPom.text.contains( '<flattenRelativePathParent>' )
assert !modulePom.text.contains( '<flattenRelativePathParent>' )
assert !childPom.text.contains( '<flattenRelativePathParent>' )
