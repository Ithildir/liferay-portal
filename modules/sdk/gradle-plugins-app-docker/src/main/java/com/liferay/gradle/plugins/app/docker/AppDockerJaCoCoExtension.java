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

import com.liferay.gradle.util.FileUtil;
import com.liferay.gradle.util.GradleUtil;
import com.liferay.gradle.util.OSDetector;

import java.io.File;

import java.util.concurrent.Callable;

import org.gradle.api.Project;

/**
 * @author Andrea Di Giorgi
 */
public class AppDockerJaCoCoExtension {

	public AppDockerJaCoCoExtension(Project project) {
		_agentJavaOpts = new Callable<String>() {

			@Override
			public String call() throws Exception {
				StringBuilder sb = new StringBuilder();

				sb.append("-javaagent:");
				sb.append(getContainerDirName());
				sb.append("/jacocoagent.jar=destfile=");
				sb.append(getContainerDirName());
				sb.append('/');
				sb.append(getDataFileName());
				sb.append(",append=true,jmx=true,output=file");

				return sb.toString();
			}

		};

		_hostDir = new Callable<File>() {

			@Override
			public File call() throws Exception {
				return new File(_project.getBuildDir(), "jacoco");
			}

		};

		_project = project;
	}

	public String getAgentJavaOpts() {
		return GradleUtil.toString(_agentJavaOpts);
	}

	public String getAgentVersion() {
		return GradleUtil.toString(_agentVersion);
	}

	public String getContainerDirName() {
		return GradleUtil.toString(_containerDirName);
	}

	public DockerCreateContainer getCreateContainerTask() {
		Object object = GradleUtil.toObject(_createContainerTask);

		if (object instanceof String) {
			object = GradleUtil.getTask(_project, (String)object);
		}

		if (object != null) {
			return (DockerCreateContainer)object;
		}

		return null;
	}

	public String getDataFileName() {
		return GradleUtil.toString(_dataFileName);
	}

	public String getHostAbsolutePath(Object object) {
		File file = GradleUtil.toFile(_project, object);

		String absolutePath = FileUtil.getAbsolutePath(file);

		if (OSDetector.isWindows()) {
			char unit = absolutePath.charAt(0);

			absolutePath =
				Character.toLowerCase(unit) + absolutePath.substring(2);
		}

		return absolutePath;
	}

	public File getHostDir() {
		return GradleUtil.toFile(_project, _hostDir);
	}

	public String getHostDirEnvironmentVariableName() {
		return GradleUtil.toString(_hostDirEnvironmentVariableName);
	}

	public void setAgentJavaOpts(Object agentJavaOpts) {
		_agentJavaOpts = agentJavaOpts;
	}

	public void setAgentVersion(Object agentVersion) {
		_agentVersion = agentVersion;
	}

	public void setContainerDirName(Object containerDirName) {
		_containerDirName = containerDirName;
	}

	public void setCreateContainerTask(Object createContainerTask) {
		_createContainerTask = createContainerTask;
	}

	public void setDataFileName(Object dataFileName) {
		_dataFileName = dataFileName;
	}

	public void setHostDir(Object hostDir) {
		_hostDir = hostDir;
	}

	public void setHostDirEnvironmentVariableName(
		Object hostDirEnvironmentVariableName) {

		_hostDirEnvironmentVariableName = hostDirEnvironmentVariableName;
	}

	private Object _agentJavaOpts;
	private Object _agentVersion = "0.7.8";
	private Object _containerDirName = "/jacoco";
	private Object _createContainerTask;
	private Object _dataFileName = "jacoco.exec";
	private Object _hostDir;
	private Object _hostDirEnvironmentVariableName = "JACOCO_DIR";
	private final Project _project;

}