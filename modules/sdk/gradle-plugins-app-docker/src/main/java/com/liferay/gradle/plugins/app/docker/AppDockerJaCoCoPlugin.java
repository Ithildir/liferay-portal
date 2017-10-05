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

package com.liferay.gradle.plugins.app.docker;

import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer;

import com.liferay.gradle.plugins.app.docker.tasks.InvokeMBeanMethodTask;
import com.liferay.gradle.util.GradleUtil;
import com.liferay.gradle.util.Validator;

import groovy.lang.Closure;

import java.io.File;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Copy;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.tasks.JacocoReport;

/**
 * @author Andrea Di Giorgi
 */
public class AppDockerJaCoCoPlugin implements Plugin<Project> {

	public static final String APP_DOCKER_JACOCO_AGENT_CONFIGURATION_NAME =
		"appDockerJaCoCoAgent";

	public static final String APP_DOCKER_JACOCO_REPORT_TASK_NAME =
		"appDockerJaCoCoReport";

	public static final String COPY_APP_DOCKER_JACOCO_AGENT_TASK_NAME =
		"copyAppDockerJaCoCoAgent";

	public static final String DUMP_APP_DOCKER_JACOCO_TASK_NAME =
		"dumpAppDockerJaCoCo";

	public static final String PLUGIN_NAME = "appDockerJaCoCo";

	@Override
	public void apply(Project project) {
		GradleUtil.applyPlugin(project, AppDockerPlugin.class);

		final AppDockerExtension appDockerExtension = GradleUtil.getExtension(
			project, AppDockerExtension.class);

		final AppDockerJaCoCoExtension appDockerJaCoCoExtension =
			GradleUtil.addExtension(
				project, PLUGIN_NAME, AppDockerJaCoCoExtension.class);

		Configuration appDockerJaCoCoAgentConfiguration =
			_addConfigurationAppDockerJaCoCoAgent(
				project, appDockerJaCoCoExtension);

		_addTaskCopyAppDockerJaCoCoAgent(
			project, appDockerJaCoCoExtension,
			appDockerJaCoCoAgentConfiguration);

		InvokeMBeanMethodTask dumpAppDockerJaCoCoDataTask =
			_addTaskDumpAppDockerJaCoCoData(project);

		final JacocoReport appDockerJaCoCoReportTask =
			_addTaskAppDockerJaCoCoReport(dumpAppDockerJaCoCoDataTask);

		project.afterEvaluate(
			new Action<Project>() {

				@Override
				public void execute(Project project) {
					_configureTaskCreateContainer(appDockerJaCoCoExtension);
				}

			});

		Gradle gradle = project.getGradle();

		gradle.afterProject(
			new Closure<Void>(project) {

				@SuppressWarnings("unused")
				public void doCall(Project subproject) {
					_configureTaskAppDockerJaCoCoReport(
						appDockerJaCoCoReportTask, appDockerExtension,
						subproject);
				}

			});

		gradle.beforeProject(
			new Closure<Void>(project) {

				@SuppressWarnings("unused")
				public void doCall(Project subproject) {
					_configureSubproject(appDockerExtension, subproject);
				}

			});
	}

	private Configuration _addConfigurationAppDockerJaCoCoAgent(
		final Project project,
		final AppDockerJaCoCoExtension appDockerJaCoCoExtension) {

		Configuration configuration = GradleUtil.addConfiguration(
			project, APP_DOCKER_JACOCO_AGENT_CONFIGURATION_NAME);

		configuration.defaultDependencies(
			new Action<DependencySet>() {

				@Override
				public void execute(DependencySet dependencySet) {
					_addDependenciesAppDockerJaCoCoAgent(
						project, appDockerJaCoCoExtension);
				}

			});

		configuration.setDescription(
			"Configures the JaCoCo Agent to use in the Docker image.");
		configuration.setVisible(false);

		return configuration;
	}

	private void _addDependenciesAppDockerJaCoCoAgent(
		Project project, AppDockerJaCoCoExtension appDockerJaCoCoExtension) {

		GradleUtil.addDependency(
			project, APP_DOCKER_JACOCO_AGENT_CONFIGURATION_NAME, "org.jacoco",
			"org.jacoco.agent", appDockerJaCoCoExtension.getAgentVersion());
	}

	private JacocoReport _addTaskAppDockerJaCoCoReport(
		InvokeMBeanMethodTask dumpAppDockerJaCoCoDataTask) {

		JacocoReport jacocoReport = GradleUtil.addTask(
			dumpAppDockerJaCoCoDataTask.getProject(),
			APP_DOCKER_JACOCO_REPORT_TASK_NAME, JacocoReport.class);

		jacocoReport.dependsOn(dumpAppDockerJaCoCoDataTask);

		return jacocoReport;
	}

	private Copy _addTaskCopyAppDockerJaCoCoAgent(
		final Project project,
		final AppDockerJaCoCoExtension appDockerJaCoCoExtension,
		Configuration appDockerJaCoCoAgentConfiguration) {

		Copy copy = GradleUtil.addTask(
			project, COPY_APP_DOCKER_JACOCO_AGENT_TASK_NAME, Copy.class);

		copy.from(appDockerJaCoCoAgentConfiguration);

		copy.into(
			new Callable<File>() {

				@Override
				public File call() throws Exception {
					return appDockerJaCoCoExtension.getHostDir();
				}

			});

		copy.setDescription(
			"Copies the JaCoCo Agent into a temporary directory to be used " +
				"by the Docker image.");

		return copy;
	}

	private InvokeMBeanMethodTask _addTaskDumpAppDockerJaCoCoData(
		Project project) {

		InvokeMBeanMethodTask invokeMBeanMethodTask = GradleUtil.addTask(
			project, DUMP_APP_DOCKER_JACOCO_TASK_NAME,
			InvokeMBeanMethodTask.class);

		invokeMBeanMethodTask.setDescription(
			"Triggers a dump of the JaCoCo execution data through the " +
				"configured output.");
		invokeMBeanMethodTask.setMethodArgs(new Object[] {Boolean.TRUE});
		invokeMBeanMethodTask.setMethodName("dump");
		invokeMBeanMethodTask.setObjectName("org.jacoco:type=Runtime");

		return invokeMBeanMethodTask;
	}

	private void _configureSubproject(
		AppDockerExtension appDockerExtension, Project subproject) {
	}

	private void _configureTaskAppDockerJaCoCoReport(
		JacocoReport jacocoReport, AppDockerExtension appDockerExtension,
		Project subproject) {

		Logger logger = jacocoReport.getLogger();

		Set<Project> subprojects = appDockerExtension.getSubprojects();

		if (!subprojects.contains(subproject)) {
			if (logger.isInfoEnabled()) {
				logger.info("Excluding {} from {}", subproject, jacocoReport);
			}

			return;
		}

		Spec<Project> spec = appDockerExtension.getOnlyIf();

		if (!spec.isSatisfiedBy(subproject)) {
			if (logger.isInfoEnabled()) {
				logger.info(
					"Explicitly excluding {} from {}", subproject,
					jacocoReport);
			}

			return;
		}

		PluginContainer pluginContainer = subproject.getPlugins();

		if (!pluginContainer.hasPlugin(JacocoPlugin.class)) {
			if (logger.isInfoEnabled()) {
				logger.info(
					"Excluding {} from {} since it does not have the " +
						"'jacoco' plugin applied",
					subproject, jacocoReport);
			}
		}
	}

	private void _configureTaskCreateContainer(
		AppDockerJaCoCoExtension appDockerJaCoCoExtension) {

		DockerCreateContainer dockerCreateContainer =
			appDockerJaCoCoExtension.getCreateContainerTask();

		if (dockerCreateContainer == null) {
			return;
		}

		String hostDirName = appDockerJaCoCoExtension.getHostAbsolutePath(
			appDockerJaCoCoExtension.getHostDir());
		String hostDirEnvironmentVariableName =
			appDockerJaCoCoExtension.getHostDirEnvironmentVariableName();

		if (Validator.isNotNull(hostDirEnvironmentVariableName)) {
			String[] env = dockerCreateContainer.getEnv();

			String[] newEnv = Arrays.copyOf(env, env.length + 1);

			newEnv[newEnv.length - 1] =
				hostDirEnvironmentVariableName + "=" + hostDirName;

			dockerCreateContainer.setEnv(newEnv);
		}

		String[] volumes = dockerCreateContainer.getVolumes();

		String[] newVolumes = Arrays.copyOf(volumes, volumes.length + 1);

		newVolumes[newVolumes.length - 1] =
			hostDirName + ":" + appDockerJaCoCoExtension.getContainerDirName();

		dockerCreateContainer.setVolumes(newVolumes);
	}

}