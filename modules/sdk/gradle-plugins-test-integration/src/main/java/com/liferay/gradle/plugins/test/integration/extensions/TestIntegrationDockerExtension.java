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

package com.liferay.gradle.plugins.test.integration.extensions;

import com.liferay.gradle.plugins.test.integration.internal.util.GradleUtil;
import com.liferay.gradle.plugins.test.integration.internal.util.TestIntegrationPluginConstants;
import com.liferay.gradle.plugins.test.integration.tasks.JmxRemotePortSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.gradle.api.Project;
import org.gradle.util.GUtil;

/**
 * @author Andrea Di Giorgi
 */
public class TestIntegrationDockerExtension implements JmxRemotePortSpec {

	public TestIntegrationDockerExtension(Project project) {
		_debug = GradleUtil.getProperty(project, "dockerDebug", false);

		_httpLocalPort = new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				return getHttpRemotePort();
			}

		};

		_jmxLocalPort = new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				return getJmxRemotePort();
			}

		};

		_jwdpLocalPort = new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				return getJwdpRemotePort();
			}

		};

		_javaDebugOpts.add("=-Xdebug");

		_javaDebugOpts.add(
			new Object() {

				@Override
				public String toString() {
					return "-Xrunjdwp:transport=dt_socket,address=" +
						getJwdpRemotePort() + ",server=y,suspend=n";
				}

			});

		_javaOpts.add("-Dcom.sun.management.jmxremote");
		_javaOpts.add("-Dcom.sun.management.jmxremote.authenticate=false");
		_javaOpts.add("-Dcom.sun.management.jmxremote.local.only=false");
		_javaOpts.add("-Dcom.sun.management.jmxremote.ssl=false");
		_javaOpts.add("-Djava.rmi.server.hostname=localhost");

		_javaOpts.add(
			new Object() {

				@Override
				public String toString() {
					return "-Dcom.sun.management.jmxremote.port=" +
						getJmxRemotePort();
				}

			});

		_javaOpts.add(
			new Object() {

				@Override
				public String toString() {
					return "-Dcom.sun.management.jmxremote.rmi.port=" +
						getJmxRemotePort();
				}

			});
	}

	public long getCheckInterval() {
		return GradleUtil.toLong(_checkInterval);
	}

	public String getCheckPath() {
		return GradleUtil.toString(_checkPath);
	}

	public long getCheckTimeout() {
		return GradleUtil.toLong(_checkTimeout);
	}

	public int getHttpLocalPort() {
		return GradleUtil.toInteger(_httpLocalPort);
	}

	public int getHttpRemotePort() {
		return GradleUtil.toInteger(_httpRemotePort);
	}

	public String getImageRepository() {
		return GradleUtil.toString(_imageRepository);
	}

	public String getImageTag() {
		return GradleUtil.toString(_imageTag);
	}

	public List<Object> getJavaDebugOpts() {
		return _javaDebugOpts;
	}

	public List<Object> getJavaOpts() {
		return _javaOpts;
	}

	public int getJmxLocalPort() {
		return GradleUtil.toInteger(_jmxLocalPort);
	}

	@Override
	public int getJmxRemotePort() {
		return GradleUtil.toInteger(_jmxRemotePort);
	}

	public int getJwdpLocalPort() {
		return GradleUtil.toInteger(_jwdpLocalPort);
	}

	public int getJwdpRemotePort() {
		return GradleUtil.toInteger(_jwdpRemotePort);
	}

	public String getTmpVolumeDirName() {
		return GradleUtil.toString(_tmpVolumeDirName);
	}

	public boolean isDebug() {
		return _debug;
	}

	public TestIntegrationDockerExtension javaDebugOpts(
		Iterable<?> javaDebugOpts) {

		GUtil.addToCollection(_javaDebugOpts, javaDebugOpts);

		return this;
	}

	public TestIntegrationDockerExtension javaDebugOpts(
		Object... javaDebugOpts) {

		return javaDebugOpts(Arrays.asList(javaDebugOpts));
	}

	public TestIntegrationDockerExtension javaOpts(Iterable<?> javaOpts) {
		GUtil.addToCollection(_javaOpts, javaOpts);

		return this;
	}

	public TestIntegrationDockerExtension javaOpts(Object... javaOpts) {
		return javaOpts(Arrays.asList(javaOpts));
	}

	public void setCheckInterval(Object checkInterval) {
		_checkInterval = checkInterval;
	}

	public void setCheckPath(Object checkPath) {
		_checkPath = checkPath;
	}

	public void setCheckTimeout(Object checkTimeout) {
		_checkTimeout = checkTimeout;
	}

	public void setDebug(boolean debug) {
		_debug = debug;
	}

	public void setHttpLocalPort(Object httpLocalPort) {
		_httpLocalPort = httpLocalPort;
	}

	public void setHttpRemotePort(Object httpRemotePort) {
		_httpRemotePort = httpRemotePort;
	}

	public void setImageRepository(Object imageRepository) {
		_imageRepository = imageRepository;
	}

	public void setImageTag(Object imageTag) {
		_imageTag = imageTag;
	}

	public void setJavaDebugOpts(Iterable<?> javaDebugOpts) {
		_javaDebugOpts.clear();

		javaDebugOpts(javaDebugOpts);
	}

	public void setJavaDebugOpts(Object... javaDebugOpts) {
		setJavaDebugOpts(Arrays.asList(javaDebugOpts));
	}

	public void setJavaOpts(Iterable<?> javaOpts) {
		_javaOpts.clear();

		javaOpts(javaOpts);
	}

	public void setJavaOpts(Object... javaOpts) {
		setJavaOpts(Arrays.asList(javaOpts));
	}

	public void setJmxLocalPort(Object jmxLocalPort) {
		_jmxLocalPort = jmxLocalPort;
	}

	@Override
	public void setJmxRemotePort(Object jmxRemotePort) {
		_jmxRemotePort = jmxRemotePort;
	}

	public void setJwdpLocalPort(Object jwdpLocalPort) {
		_jwdpLocalPort = jwdpLocalPort;
	}

	public void setJwdpRemotePort(Object jwdpRemotePort) {
		_jwdpRemotePort = jwdpRemotePort;
	}

	public void setTmpVolumeDirName(Object tmpVolumeDirName) {
		_tmpVolumeDirName = tmpVolumeDirName;
	}

	private Object _checkInterval =
		TestIntegrationPluginConstants.DEFAULT_CHECK_INTERVAL;
	private Object _checkPath =
		TestIntegrationPluginConstants.DEFAULT_CHECK_PATH;
	private Object _checkTimeout =
		TestIntegrationPluginConstants.DEFAULT_CHECK_TIMEOUT;
	private boolean _debug;
	private Object _httpLocalPort;
	private Object _httpRemotePort =
		TestIntegrationPluginConstants.DEFAULT_PORT_HTTP;
	private Object _imageRepository;
	private Object _imageTag = "latest";
	private final List<Object> _javaDebugOpts = new ArrayList<>();
	private final List<Object> _javaOpts = new ArrayList<>();
	private Object _jmxLocalPort;
	private Object _jmxRemotePort =
		TestIntegrationPluginConstants.DEFAULT_PORT_JMX;
	private Object _jwdpLocalPort;
	private Object _jwdpRemotePort =
		TestIntegrationPluginConstants.DEFAULT_PORT_JWDP;
	private Object _tmpVolumeDirName = "tmpdir";

}