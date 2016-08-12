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

package com.liferay.gradle.plugins.css.builder;

import com.liferay.gradle.util.GradleUtil;

import java.io.File;

import java.util.concurrent.Callable;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.TaskContainer;

/**
 * @author Andrea Di Giorgi
 */
public class CSSBuilderPlugin implements Plugin<Project> {

	public static final String BUILD_CSS_TASK_NAME = "buildCSS";

	public static final String CSS_BUILDER_CONFIGURATION_NAME = "cssBuilder";

	public static final String PORTAL_COMMON_CSS_CONFIGURATION_NAME =
		"portalCommonCSS";

	@Override
	public void apply(Project project) {
		Configuration cssBuilderConfiguration = addConfigurationCSSBuilder(
			project);
		Configuration portalCommonCSSConfiguration =
			addConfigurationPortalCommonCSS(project);

		addTaskBuildCSS(project);

		configureTasksBuildCSS(
			project, cssBuilderConfiguration, portalCommonCSSConfiguration);
	}

	protected Configuration addConfigurationCSSBuilder(final Project project) {
		Configuration configuration = GradleUtil.addConfiguration(
			project, CSS_BUILDER_CONFIGURATION_NAME);

		configuration.defaultDependencies(
			new Action<DependencySet>() {

				@Override
				public void execute(DependencySet dependencySet) {
					addDependenciesCSSBuilder(project);
				}

			});

		configuration.setDescription(
			"Configures Liferay CSS Builder for this project.");
		configuration.setVisible(false);

		return configuration;
	}

	protected Configuration addConfigurationPortalCommonCSS(
		final Project project) {

		Configuration configuration = GradleUtil.addConfiguration(
			project, PORTAL_COMMON_CSS_CONFIGURATION_NAME);

		configuration.defaultDependencies(
			new Action<DependencySet>() {

				@Override
				public void execute(DependencySet dependencySet) {
					addDependenciesPortalCommonCSS(project);
				}

			});

		configuration.setDescription(
			"Configures com.liferay.frontend.css.common for compiling CSS " +
				"files.");
		configuration.setTransitive(false);
		configuration.setVisible(false);

		return configuration;
	}

	protected void addDependenciesCSSBuilder(Project project) {
		GradleUtil.addDependency(
			project, CSS_BUILDER_CONFIGURATION_NAME, "com.liferay",
			"com.liferay.css.builder", "latest.release");
	}

	protected void addDependenciesPortalCommonCSS(Project project) {
		GradleUtil.addDependency(
			project, PORTAL_COMMON_CSS_CONFIGURATION_NAME, "com.liferay",
			"com.liferay.frontend.css.common", "latest.release", false);
	}

	protected BuildCSSTask addTaskBuildCSS(Project project) {
		final BuildCSSTask buildCSSTask = GradleUtil.addTask(
			project, BUILD_CSS_TASK_NAME, BuildCSSTask.class);

		buildCSSTask.setDescription("Build CSS files.");
		buildCSSTask.setGroup(BasePlugin.BUILD_GROUP);

		PluginContainer pluginContainer = project.getPlugins();

		pluginContainer.withType(
			JavaPlugin.class,
			new Action<JavaPlugin>() {

				@Override
				public void execute(JavaPlugin javaPlugin) {
					configureTaskBuildCSSForJavaPlugin(buildCSSTask);
				}

			});

		pluginContainer.withType(
			WarPlugin.class,
			new Action<WarPlugin>() {

				@Override
				public void execute(WarPlugin warPlugin) {
					configureTaskBuildCSSForWarPlugin(buildCSSTask);
				}

			});

		return buildCSSTask;
	}

	protected void configureTaskBuildCSSClasspath(
		BuildCSSTask buildCSSTask, FileCollection classpath) {

		buildCSSTask.setClasspath(classpath);
	}

	protected void configureTaskBuildCSSForJavaPlugin(
		final BuildCSSTask buildCSSTask) {

		buildCSSTask.setDocrootDir(
			new Callable<File>() {

				@Override
				public File call() throws Exception {
					return GradleUtil.getMainResourcesDir(
						buildCSSTask.getProject());
				}

			});

		Task processResourcesTask = GradleUtil.getTask(
			buildCSSTask.getProject(), JavaPlugin.PROCESS_RESOURCES_TASK_NAME);

		processResourcesTask.dependsOn(buildCSSTask);
	}

	protected void configureTaskBuildCSSForWarPlugin(
		final BuildCSSTask buildCSSTask) {

		buildCSSTask.setDocrootDir(
			new Callable<File>() {

				@Override
				public File call() throws Exception {
					return GradleUtil.getWebAppDir(buildCSSTask.getProject());
				}

			});
	}

	protected void configureTaskBuildCSSPortalCommonFile(
		BuildCSSTask buildCSSTask,
		final Configuration portalCommonCSSConfiguration) {

		buildCSSTask.setPortalCommonFile(
			new Callable<File>() {

				@Override
				public File call() throws Exception {
					return portalCommonCSSConfiguration.getSingleFile();
				}

			});
	}

	protected void configureTasksBuildCSS(
		Project project, final Configuration cssBuilderConfiguration,
		final Configuration portalCommonCSSConfiguration) {

		TaskContainer taskContainer = project.getTasks();

		taskContainer.withType(
			BuildCSSTask.class,
			new Action<BuildCSSTask>() {

				@Override
				public void execute(BuildCSSTask buildCSSTask) {
					configureTaskBuildCSSClasspath(
						buildCSSTask, cssBuilderConfiguration);
					configureTaskBuildCSSPortalCommonFile(
						buildCSSTask, portalCommonCSSConfiguration);
				}

			});
	}

}