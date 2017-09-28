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
import com.liferay.gradle.plugins.css.builder.BuildCSSTask;
import com.liferay.gradle.plugins.css.builder.CSSBuilderPlugin;
import com.liferay.gradle.plugins.defaults.internal.util.GradleUtil;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Andrea Di Giorgi
 */
public class CSSBuilderDefaultsPlugin
	extends BaseDefaultsPlugin<CSSBuilderPlugin> {

	public static final Plugin<Project> INSTANCE =
		new CSSBuilderDefaultsPlugin();

	@Override
	protected void configureDefaults(
		Project project, CSSBuilderPlugin cssBuilderPlugin) {

		_configureTaskBuildCSS(project);
	}

	@Override
	protected Class<CSSBuilderPlugin> getPluginClass() {
		return CSSBuilderPlugin.class;
	}

	private CSSBuilderDefaultsPlugin() {
	}

	private void _configureTaskBuildCSS(Project project) {
		BuildCSSTask buildCSSTask = (BuildCSSTask)GradleUtil.getTask(
			project, CSSBuilderPlugin.BUILD_CSS_TASK_NAME);

		buildCSSTask.setFork(false);
	}

}