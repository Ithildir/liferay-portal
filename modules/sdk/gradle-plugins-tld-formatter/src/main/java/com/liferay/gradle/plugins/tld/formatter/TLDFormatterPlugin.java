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

package com.liferay.gradle.plugins.tld.formatter;

import com.liferay.gradle.util.GradleUtil;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskContainer;

/**
 * @author Andrea Di Giorgi
 */
public class TLDFormatterPlugin implements Plugin<Project> {

	public static final String CONFIGURATION_NAME = "tldFormatter";

	public static final String FORMAT_TLD_TASK_NAME = "formatTLD";

	@Override
	public void apply(Project project) {
		Configuration tldFormatterConfiguration = _addConfigurationTLDFormatter(
			project);

		_addTaskFormatTLD(project);

		_configureTasksFormatTLD(project, tldFormatterConfiguration);
	}

	private Configuration _addConfigurationTLDFormatter(final Project project) {
		Configuration configuration = GradleUtil.addConfiguration(
			project, CONFIGURATION_NAME);

		configuration.defaultDependencies(
			new Action<DependencySet>() {

				@Override
				public void execute(DependencySet dependencySet) {
					_addDependenciesTLDFormatter(project);
				}

			});

		configuration.setDescription(
			"Configures Liferay TLD Formatter for this project.");
		configuration.setVisible(false);

		return configuration;
	}

	private void _addDependenciesTLDFormatter(Project project) {
		GradleUtil.addDependency(
			project, CONFIGURATION_NAME, "com.liferay",
			"com.liferay.tld.formatter", "latest.release");
	}

	private FormatTLDTask _addTaskFormatTLD(Project project) {
		FormatTLDTask formatTLDTask = GradleUtil.addTask(
			project, FORMAT_TLD_TASK_NAME, FormatTLDTask.class);

		formatTLDTask.setDescription(
			"Runs Liferay TLD Formatter to format files.");

		return formatTLDTask;
	}

	private void _configureTaskFormatTLDClasspath(
		FormatTLDTask formatTLDTask, FileCollection fileCollection) {

		formatTLDTask.setClasspath(fileCollection);
	}

	private void _configureTasksFormatTLD(
		Project project, final Configuration tldFormatterConfiguration) {

		TaskContainer taskContainer = project.getTasks();

		taskContainer.withType(
			FormatTLDTask.class,
			new Action<FormatTLDTask>() {

				@Override
				public void execute(FormatTLDTask formatTLDTask) {
					_configureTaskFormatTLDClasspath(
						formatTLDTask, tldFormatterConfiguration);
				}

			});
	}

}