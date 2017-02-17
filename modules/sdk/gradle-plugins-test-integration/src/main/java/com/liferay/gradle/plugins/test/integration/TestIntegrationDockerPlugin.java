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

package com.liferay.gradle.plugins.test.integration;

import com.bmuschko.gradle.docker.DockerRemoteApiPlugin;
import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer;
import com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer;
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer;
import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer;
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage;

import com.liferay.gradle.plugins.test.integration.extensions.TestIntegrationDockerExtension;
import com.liferay.gradle.plugins.test.integration.internal.util.ArrayUtil;
import com.liferay.gradle.plugins.test.integration.internal.util.GradleUtil;
import com.liferay.gradle.util.FileUtil;
import com.liferay.gradle.util.OSDetector;
import com.liferay.gradle.util.Validator;

import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.tasks.testing.Test;
import org.gradle.util.CollectionUtils;

/**
 * @author Andrea Di Giorgi
 */
public class TestIntegrationDockerPlugin implements Plugin<Project> {

	public static final String CREATE_TEST_CONTAINER_TASK_NAME =
		"createTestContainer";

	public static final String PULL_TEST_IMAGE_TASK_NAME = "pullTestImage";

	public static final String REMOVE_TEST_CONTAINER_TASK_NAME =
		"removeTestContainer";

	public static final String START_TEST_CONTAINER_TASK_NAME =
		"startTestContainer";

	public static final String STOP_TEST_CONTAINER_TASK_NAME =
		"stopTestContainer";

	@Override
	public void apply(Project project) {
		GradleUtil.applyPlugin(project, DockerRemoteApiPlugin.class);
		GradleUtil.applyPlugin(project, TestIntegrationBasePlugin.class);

		Test test = (Test)GradleUtil.getTask(
			project, TestIntegrationBasePlugin.TEST_INTEGRATION_TASK_NAME);

		if (GradleUtil.hasPlugin(project, TestIntegrationPlugin.class)) {
			throw new GradleException(
				"Unable to apply both \"com.liferay.test.integration\" and \"" +
					"com.liferay.test.integration.docker\"");
		}

		TestIntegrationDockerExtension testIntegrationDockerExtension =
			GradleUtil.addExtension(
				project, TestIntegrationPlugin.PLUGIN_NAME + "Docker",
				TestIntegrationDockerExtension.class);

		DockerPullImage dockerPullImage = _addTaskPullTestImage(
			project, testIntegrationDockerExtension);

		DockerCreateContainer dockerCreateContainer =
			_addTaskCreateTestContainer(
				dockerPullImage, testIntegrationDockerExtension, test);

		DockerStartContainer dockerStartContainer = _addTaskStartTestContainer(
			dockerCreateContainer, testIntegrationDockerExtension);

		DockerStopContainer dockerStopContainer = _addTaskStopTestContainer(
			dockerStartContainer);

		DockerRemoveContainer dockerRemoveContainer =
			_addTaskRemoveTestContainer(dockerStopContainer);

		_configureTaskTestIntegration(
			test, dockerStartContainer, dockerRemoveContainer);
	}

	private DockerCreateContainer _addTaskCreateTestContainer(
		final DockerPullImage dockerPullImage,
		final TestIntegrationDockerExtension testIntegrationDockerExtension,
		final Test test) {

		Project project = dockerPullImage.getProject();

		final DockerCreateContainer dockerCreateContainer = GradleUtil.addTask(
			project, CREATE_TEST_CONTAINER_TASK_NAME,
			DockerCreateContainer.class);

		dockerCreateContainer.dependsOn(dockerPullImage);
		dockerCreateContainer.setContainerName(project.getName());
		dockerCreateContainer.setDescription(
			"Creates the Docker container that runs the integration tests of " +
				"this project.");

		dockerCreateContainer.targetImageId(
			new Closure<String>(dockerCreateContainer) {

				@SuppressWarnings("unused")
				public String doCall() {
					String repository = dockerPullImage.getRepository();
					String tag = dockerPullImage.getTag();

					if (Validator.isNull(repository) || Validator.isNull(tag)) {
						return null;
					}

					return repository + ":" + tag;
				}

			});

		project.afterEvaluate(
			new Action<Project>() {

				@Override
				public void execute(Project project) {
					dockerCreateContainer.setEnv(
						_getTestContainerEnv(
							dockerCreateContainer,
							testIntegrationDockerExtension));
					dockerCreateContainer.setPortBindings(
						_getTestContainerPortBindings(
							dockerCreateContainer,
							testIntegrationDockerExtension));
					dockerCreateContainer.setVolumes(
						_getTestContainerVolumes(
							dockerCreateContainer,
							testIntegrationDockerExtension, test));
				}

			});

		return dockerCreateContainer;
	}

	private DockerPullImage _addTaskPullTestImage(
		Project project,
		final TestIntegrationDockerExtension testIntegrationDockerExtension) {

		final DockerPullImage dockerPullImage = GradleUtil.addTask(
			project, PULL_TEST_IMAGE_TASK_NAME, DockerPullImage.class);

		dockerPullImage.setDescription(
			"Pulls the Docker image that runs the integration tests of this " +
				"project.");

		project.afterEvaluate(
			new Action<Project>() {

				@Override
				public void execute(Project project) {
					if (Validator.isNull(dockerPullImage.getRepository())) {
						dockerPullImage.setRepository(
							testIntegrationDockerExtension.
								getImageRepository());
					}

					if (Validator.isNull(dockerPullImage.getTag())) {
						dockerPullImage.setTag(
							testIntegrationDockerExtension.getImageTag());
					}
				}

			});

		return dockerPullImage;
	}

	private DockerRemoveContainer _addTaskRemoveTestContainer(
		final DockerStopContainer dockerStopContainer) {

		DockerRemoveContainer dockerRemoveContainer = GradleUtil.addTask(
			dockerStopContainer.getProject(), REMOVE_TEST_CONTAINER_TASK_NAME,
			DockerRemoveContainer.class);

		dockerRemoveContainer.dependsOn(dockerStopContainer);
		dockerRemoveContainer.setDescription(
			"Removes the Docker container that runs the integration tests of " +
				"this project.");

		dockerRemoveContainer.targetContainerId(
			new Closure<String>(dockerRemoveContainer) {

				@SuppressWarnings("unused")
				public String doCall() {
					return dockerStopContainer.getContainerId();
				}

			});

		return dockerRemoveContainer;
	}

	private DockerStartContainer _addTaskStartTestContainer(
		final DockerCreateContainer dockerCreateContainer,
		final TestIntegrationDockerExtension testIntegrationDockerExtension) {

		DockerStartContainer dockerStartContainer = GradleUtil.addTask(
			dockerCreateContainer.getProject(), START_TEST_CONTAINER_TASK_NAME,
			DockerStartContainer.class);

		dockerStartContainer.dependsOn(dockerCreateContainer);

		dockerStartContainer.doLast(
			new Action<Task>() {

				@Override
				public void execute(Task task) {
					GradleUtil.waitFor(
						new Callable<Boolean>() {

							@Override
							public Boolean call() throws Exception {
								return GradleUtil.isReachable(
									"http", "localhost",
									testIntegrationDockerExtension.
										getHttpLocalPort(),
									testIntegrationDockerExtension.
										getCheckPath());
							}

						},
						testIntegrationDockerExtension.getCheckInterval(),
						testIntegrationDockerExtension.getCheckTimeout(),
						"Docker container");
				}

			});

		dockerStartContainer.setDescription(
			"Starts the Docker container that runs the integration tests of " +
				"this project.");

		dockerStartContainer.targetContainerId(
			new Closure<String>(dockerStartContainer) {

				@SuppressWarnings("unused")
				public String doCall() {
					return dockerCreateContainer.getContainerId();
				}

			});

		return dockerStartContainer;
	}

	private DockerStopContainer _addTaskStopTestContainer(
		final DockerStartContainer dockerStartContainer) {

		DockerStopContainer dockerStopContainer = GradleUtil.addTask(
			dockerStartContainer.getProject(), STOP_TEST_CONTAINER_TASK_NAME,
			DockerStopContainer.class);

		dockerStopContainer.setDescription(
			"Stops the Docker container that runs the integration tests of " +
				"this project.");

		dockerStopContainer.targetContainerId(
			new Closure<String>(dockerStopContainer) {

				@SuppressWarnings("unused")
				public String doCall() {
					return dockerStartContainer.getContainerId();
				}

			});

		return dockerStopContainer;
	}

	private void _configureTaskTestIntegration(
		Test test, DockerStartContainer dockerStartContainer,
		DockerRemoveContainer dockerRemoveContainer) {

		test.dependsOn(dockerStartContainer);
		test.finalizedBy(dockerRemoveContainer);
	}

	private String[] _getTestContainerEnv(
		DockerCreateContainer dockerCreateContainer,
		TestIntegrationDockerExtension testIntegrationDockerExtension) {

		List<Object> javaOpts = new ArrayList<>(
			testIntegrationDockerExtension.getJavaOpts());

		if (testIntegrationDockerExtension.isDebug()) {
			javaOpts.addAll(testIntegrationDockerExtension.getJavaDebugOpts());
		}

		if (javaOpts.isEmpty()) {
			return dockerCreateContainer.getEnv();
		}

		List<String> env = ArrayUtil.toList(dockerCreateContainer.getEnv());

		env.add("JAVA_OPTS=" + CollectionUtils.join(" ", javaOpts));

		return env.toArray(new String[env.size()]);
	}

	private List<String> _getTestContainerPortBindings(
		DockerCreateContainer dockerCreateContainer,
		TestIntegrationDockerExtension testIntegrationDockerExtension) {

		List<String> portBindings = new ArrayList<>();

		List<String> dockerCreateContainerPortBindings =
			dockerCreateContainer.getPortBindings();

		if (dockerCreateContainerPortBindings != null) {
			portBindings.addAll(dockerCreateContainerPortBindings);
		}

		portBindings.add(
			testIntegrationDockerExtension.getHttpLocalPort() + ":" +
				testIntegrationDockerExtension.getHttpRemotePort());

		portBindings.add(
			testIntegrationDockerExtension.getJmxLocalPort() + ":" +
				testIntegrationDockerExtension.getJmxRemotePort());

		if (testIntegrationDockerExtension.isDebug()) {
			portBindings.add(
				testIntegrationDockerExtension.getJwdpLocalPort() + ":" +
					testIntegrationDockerExtension.getJwdpRemotePort());
		}

		return portBindings;
	}

	private String[] _getTestContainerVolumes(
		DockerCreateContainer dockerCreateContainer,
		TestIntegrationDockerExtension testIntegrationDockerExtension,
		Test test) {

		File tmpVolumeDir = _getTmpVolumeDir(
			testIntegrationDockerExtension, test);

		if (tmpVolumeDir == null) {
			return dockerCreateContainer.getVolumes();
		}

		List<String> volumes = ArrayUtil.toList(
			dockerCreateContainer.getVolumes());

		String tmpVolumeDirName = FileUtil.getAbsolutePath(tmpVolumeDir);

		if (OSDetector.isWindows()) {
			tmpVolumeDirName = tmpVolumeDirName.substring(2);
		}

		volumes.add(tmpVolumeDirName + ":" + tmpVolumeDirName);

		return volumes.toArray(new String[volumes.size()]);
	}

	private File _getTmpVolumeDir(
		TestIntegrationDockerExtension testIntegrationDockerExtension,
		Test test) {

		String dirName = testIntegrationDockerExtension.getTmpVolumeDirName();

		if (Validator.isNull(dirName)) {
			return null;
		}

		File dir = new File(test.getTemporaryDir(), dirName);

		Path dirPath = dir.toPath();

		try {
			if (OSDetector.isWindows()) {
				Files.createDirectories(dirPath);
			}
			else {
				Files.createDirectories(dirPath, _tmpVolumeDirAttribute);
			}
		}
		catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}

		return dir;
	}

	private static final FileAttribute<Set<PosixFilePermission>>
		_tmpVolumeDirAttribute = PosixFilePermissions.asFileAttribute(
			PosixFilePermissions.fromString("rwxrwxrwx"));

}