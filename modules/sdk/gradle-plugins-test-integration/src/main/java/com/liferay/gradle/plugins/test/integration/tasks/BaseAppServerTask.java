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

package com.liferay.gradle.plugins.test.integration.tasks;

import com.liferay.gradle.plugins.test.integration.internal.util.GradleUtil;
import com.liferay.gradle.plugins.test.integration.internal.util.TestIntegrationPluginConstants;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.util.GUtil;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

/**
 * @author Andrea Di Giorgi
 */
public abstract class BaseAppServerTask extends DefaultTask {

	public void executableArgs(Iterable<?> executableArgs) {
		GUtil.addToCollection(_executableArgs, executableArgs);
	}

	public void executableArgs(Object... executableArgs) {
		executableArgs(Arrays.asList(executableArgs));
	}

	@Input
	public File getBinDir() {
		return GradleUtil.toFile(getProject(), _binDir);
	}

	@Input
	public long getCheckInterval() {
		return _checkInterval;
	}

	@Input
	public String getCheckPath() {
		return GradleUtil.toString(_checkPath);
	}

	@Input
	public long getCheckTimeout() {
		return _checkTimeout;
	}

	@Input
	public String getExecutable() {
		return GradleUtil.toString(_executable);
	}

	@Input
	public List<String> getExecutableArgs() {
		List<Object> executableArgs = new ArrayList<>(_executableArgs.size());

		for (Object object : _executableArgs) {
			executableArgs.add(GradleUtil.toObject(object));
		}

		executableArgs = GUtil.flatten(executableArgs);

		return GradleUtil.toStringList(executableArgs);
	}

	@Input
	public int getPortNumber() {
		return GradleUtil.toInteger(_portNumber);
	}

	public boolean isReachable() {
		return GradleUtil.isReachable(
			"http", "localhost", getPortNumber(), getCheckPath());
	}

	public void setBinDir(Object binDir) {
		_binDir = binDir;
	}

	public void setCheckInterval(long checkInterval) {
		_checkInterval = checkInterval;
	}

	public void setCheckPath(Object checkPath) {
		_checkPath = checkPath;
	}

	public void setCheckTimeout(long checkTimeout) {
		_checkTimeout = checkTimeout;
	}

	public void setExecutable(Object executable) {
		_executable = executable;
	}

	public void setExecutableArgs(Iterable<?> executableArgs) {
		_executableArgs.clear();

		executableArgs(executableArgs);
	}

	public void setExecutableArgs(Object... executableArgs) {
		setExecutableArgs(Arrays.asList(executableArgs));
	}

	public void setPortNumber(Object portNumber) {
		_portNumber = portNumber;
	}

	protected ProcessExecutor getProcessExecutor() {
		List<String> commands = new ArrayList<>();

		File executableFile = new File(getBinDir(), getExecutable());

		commands.add(executableFile.getAbsolutePath());

		commands.addAll(getExecutableArgs());

		ProcessExecutor processExecutor = new ProcessExecutor(commands);

		processExecutor.directory(getBinDir());

		Slf4jStream slf4jStream = Slf4jStream.ofCaller();

		processExecutor.redirectOutput(slf4jStream.asWarn());

		return processExecutor;
	}

	protected void waitFor(Callable<Boolean> callable) {
		GradleUtil.waitFor(
			callable, getCheckInterval(), getCheckTimeout(),
			"application server");
	}

	private Object _binDir;
	private long _checkInterval =
		TestIntegrationPluginConstants.DEFAULT_CHECK_INTERVAL;
	private Object _checkPath;
	private long _checkTimeout =
		TestIntegrationPluginConstants.DEFAULT_CHECK_TIMEOUT;
	private Object _executable;
	private final List<Object> _executableArgs = new ArrayList<>();
	private Object _portNumber;

}