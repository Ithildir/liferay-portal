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
import com.liferay.gradle.plugins.LiferayBasePlugin;
import com.liferay.gradle.plugins.defaults.LiferayOSGiDefaultsPlugin;
import com.liferay.gradle.plugins.defaults.internal.util.FileUtil;
import com.liferay.gradle.plugins.defaults.internal.util.GradleUtil;
import com.liferay.gradle.plugins.test.integration.TestIntegrationBasePlugin;
import com.liferay.gradle.plugins.test.integration.TestIntegrationPlugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;

/**
 * @author Andrea Di Giorgi
 */
public class LiferayOSGiTestIntegrationDefaultsPlugin
	extends BaseDefaultsPlugin<TestIntegrationPlugin> {

	public static final Plugin<Project> INSTANCE =
		new LiferayOSGiTestIntegrationDefaultsPlugin();

	@Override
	protected void configureDefaults(
		Project project, TestIntegrationPlugin testIntegrationPlugin) {

		Configuration portalConfiguration = GradleUtil.getConfiguration(
			project, LiferayBasePlugin.PORTAL_CONFIGURATION_NAME);
		Configuration portalTestConfiguration = GradleUtil.getConfiguration(
			project, LiferayOSGiDefaultsPlugin.PORTAL_TEST_CONFIGURATION_NAME);

		_configureSourceSetTestIntegration(
			project, portalConfiguration, portalTestConfiguration);
	}

	@Override
	protected Class<TestIntegrationPlugin> getPluginClass() {
		return TestIntegrationPlugin.class;
	}

	private LiferayOSGiTestIntegrationDefaultsPlugin() {
	}

	private void _configureSourceSetTestIntegration(
		Project project, Configuration portalConfiguration,
		Configuration portalTestConfiguration) {

		SourceSet sourceSet = GradleUtil.getSourceSet(
			project,
			TestIntegrationBasePlugin.TEST_INTEGRATION_SOURCE_SET_NAME);

		sourceSet.setCompileClasspath(
			FileUtil.join(
				portalConfiguration, sourceSet.getCompileClasspath(),
				portalTestConfiguration));

		sourceSet.setRuntimeClasspath(
			FileUtil.join(
				portalConfiguration, sourceSet.getRuntimeClasspath(),
				portalTestConfiguration));
	}

}