package org.codehaus.mojo.flatten;

import static org.junit.Assert.*;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.DefaultPlexusConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test-Case for {@link FlattenMojo}.
 *
 */
public class KeepCommentsInPomTest
{

	private static final String PATH = "src/test/resources/keep-comments-in-pom/";
	private static final String TEST_TARGET_PATH = "target/test/resources/keep-comments-in-pom/";
	private static final String FLATTENED_POM = TEST_TARGET_PATH + ".flattened-pom.xml";
	private static final String EXPECTED_FLATTENED_POM = PATH + "expected-flattened-pom.xml";
	/**
	 * Expected result since jdk11 with updated xml header and properties sequence.
	 */
	private static final String EXPECTED_FLATTENED_POM_JDK11 = PATH + "expected-flattened-pom-jdk11.xml";

	@Rule
	public MojoRule rule = new MojoRule();

	@Before
	public void setup()
	{
		new File(TEST_TARGET_PATH).mkdirs();
	}

	/**
	 * Test method to check that profile activation file is not interpolated.
	 *
	 * @throws Exception if something goes wrong.
	 */
	@Test
	public void keepsProfileActivationFile() throws Exception
	{
		MavenProject project = rule.readMavenProject(new File(PATH));
		FlattenMojo flattenMojo = (FlattenMojo) rule.lookupConfiguredMojo(project, "flatten");

		DefaultPlexusConfiguration tempPluginConfiguration = new DefaultPlexusConfiguration("test");
		tempPluginConfiguration.addChild("outputDirectory", TEST_TARGET_PATH);
		tempPluginConfiguration.addChild("keepCommentsInPom", "true");
		rule.configureMojo(flattenMojo, tempPluginConfiguration);

		// execute writes new FLATTENED_POM
		flattenMojo.execute();

		String tempExpectedContent;
		if (isJdk8())
		{
			tempExpectedContent = getContent(EXPECTED_FLATTENED_POM);
		} else
		{
			tempExpectedContent = getContent(EXPECTED_FLATTENED_POM_JDK11);
		}
		String tempActualContent = getContent(FLATTENED_POM);
		assertEquals("Expected POM does not match, see " + FLATTENED_POM, tempExpectedContent, tempActualContent);

	}

	/**
	 * Check runtime version.
	 * 
	 * @return true when runtime is JDK11
	 */
	private boolean isJdk8()
	{
		// With Java 9 can be switched to java.lang.Runtime.version()
		String tempPropertyVersion = System.getProperty("java.version");
		if (tempPropertyVersion.startsWith("1.8."))
		{
			return true;
		}
		return false;
	}

	/**
	 * 
	 */
	private String getContent(String aPomFile) throws IOException
	{
		String tempString;
		try (InputStream tempIn = new FileInputStream(aPomFile))
		{
			tempString = IOUtils.toString(tempIn);
		}
		// remove platform dependent CR/LF
		tempString = tempString.replaceAll("\r\n", "\n");
		return tempString;
	}

}
