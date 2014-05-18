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

def originalPomContent = originalPom.getText('UTF-8')
def originalHeader = originalPomContent.substring(0, originalPomContent.indexOf('<project'))
def commentEndIndex = originalHeader.indexOf('-->')
assert commentEndIndex > 0;
def commentStartIndex = originalHeader.indexOf('<!--')
assert commentStartIndex > 0;
originalHeader = originalHeader.substring(commentStartIndex, commentEndIndex).replaceAll('\r','')
assert originalHeader.contains('Licensed to the Apache Software Foundation (ASF)')
assert originalHeader.contains('http://www.apache.org/licenses/LICENSE-2.0')
assert originalHeader.contains('under the License.')

File flattendPom = new File( basedir, 'flattened-pom.xml' )
assert flattendPom.exists()

def flattendPomContent = flattendPom.getText('UTF-8')
def consumerHeader = flattendPomContent.substring(0, flattendPomContent.indexOf('<project'))
commentEndIndex = consumerHeader.indexOf('-->')
assert commentEndIndex > 0
commentStartIndex = consumerHeader.indexOf('<!--')
assert commentStartIndex > 0;
consumerHeader = consumerHeader.substring(commentStartIndex, commentEndIndex).replaceAll('\r','')
assert consumerHeader == originalHeader
