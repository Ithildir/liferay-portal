/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.gradle.plugins.defaults.internal;

import com.liferay.gradle.plugins.BaseDefaultsPlugin;
import com.liferay.gradle.plugins.defaults.LiferayOSGiDefaultsPlugin;
import com.liferay.gradle.plugins.defaults.internal.util.GradleUtil;
import com.liferay.gradle.plugins.test.integration.TestIntegrationBasePlugin;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.testing.JUnitXmlReport;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestTaskReports;

/**
 * @author Andrea Di Giorgi
 */
public class LiferayOSGiTestIntegrationBaseDefaultsPlugin
	extends BaseDefaultsPlugin<TestIntegrationBasePlugin> {

	public static final Plugin<Project> INSTANCE =
		new LiferayOSGiTestIntegrationBaseDefaultsPlugin();

	@Override
	protected void configureDefaults(
		Project project, TestIntegrationBasePlugin testIntegrationBasePlugin) {

		SourceSet sourceSet = GradleUtil.getSourceSet(
			project,
			TestIntegrationBasePlugin.TEST_INTEGRATION_SOURCE_SET_NAME);

		_configureSourceSetTestIntegration(project, sourceSet);
		_configureTaskJar(project, sourceSet);
		_configureTaskJarSources(project, sourceSet);

		_configureTaskTestIntegration(project);
	}

	@Override
	protected Class<TestIntegrationBasePlugin> getPluginClass() {
		return TestIntegrationBasePlugin.class;
	}

	private LiferayOSGiTestIntegrationBaseDefaultsPlugin() {
	}

	private void _configureSourceSetTestIntegration(
		Project project, SourceSet sourceSet) {

		SourceSetOutput sourceSetOutput = sourceSet.getOutput();

		File dir = project.file("test-classes/integration");

		sourceSetOutput.setClassesDir(dir);
		sourceSetOutput.setResourcesDir(dir);
	}

	private void _configureTaskJar(Project project, SourceSet sourceSet) {
		Jar jar = (Jar)GradleUtil.getTask(project, JavaPlugin.JAR_TASK_NAME);

		jar.dependsOn(sourceSet.getClassesTaskName());
	}

	private void _configureTaskJarSources(
		Project project, SourceSet sourceSet) {

		Jar jar = (Jar)GradleUtil.getTask(
			project, LiferayOSGiDefaultsPlugin.JAR_SOURCES_TASK_NAME);

		jar.from(sourceSet.getAllSource());
	}

	private void _configureTaskTestIntegration(Project project) {
		Test test = (Test)GradleUtil.getTask(
			project, TestIntegrationBasePlugin.TEST_INTEGRATION_TASK_NAME);

		test.systemProperty("org.apache.maven.offline", Boolean.TRUE);

		File resultsDir = project.file("test-results/integration");

		test.setBinResultsDir(new File(resultsDir, "binary/testIntegration"));

		TestTaskReports testTaskReports = test.getReports();

		JUnitXmlReport jUnitXmlReport = testTaskReports.getJunitXml();

		jUnitXmlReport.setDestination(resultsDir);
	}

}