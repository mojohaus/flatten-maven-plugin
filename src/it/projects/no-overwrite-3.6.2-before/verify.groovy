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
String mavenVersion = "./mvnw -v".execute().text.split()[2]
assert "3.6.3" == mavenVersion
String[] mavenVersionArray = mavenVersion.split("\\.")
int[] versionArray = new int[3]
for (int i = 0; i < 3; i++)
    versionArray[i] = Integer.valueOf(mavenVersionArray[i])
boolean isValid = versionArray[0] < 3\
 || versionArray[0] == 3 && versionArray[1] < 6\
 || versionArray[0] == 3 && versionArray[1] == 6 && versionArray[2] < 3
if (isValid) {
    File flattendPom = new File( basedir, '.flattened-pom.xml' )
    assert flattendPom.exists()
    long now = System.currentTimeMillis()
    assert now - flattendPom.lastModified() > 20*1000
}


