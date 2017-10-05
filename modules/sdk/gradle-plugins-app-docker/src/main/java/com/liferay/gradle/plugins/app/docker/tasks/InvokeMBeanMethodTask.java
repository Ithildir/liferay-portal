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

package com.liferay.gradle.plugins.app.docker.tasks;

import com.liferay.gradle.util.GradleUtil;

import groovy.util.GroovyMBean;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

/**
 * @author Andrea Di Giorgi
 */
public class InvokeMBeanMethodTask extends DefaultTask {

	@Input
	public String getJmxHostName() {
		return GradleUtil.toString(_jmxHostName);
	}

	@Input
	public int getJmxPort() {
		return GradleUtil.toInteger(_jmxPort);
	}

	@Input
	@Optional
	public Object getMethodArgs() {
		return _methodArgs;
	}

	@Input
	public String getMethodName() {
		return GradleUtil.toString(_methodName);
	}

	public Object getMethodResult() {
		return _methodResult;
	}

	@Input
	public String getObjectName() {
		return GradleUtil.toString(_objectName);
	}

	@TaskAction
	public void invokeMBeanMethod() throws Exception {
		JMXServiceURL jmxServiceURL = new JMXServiceURL(
			"service:jmx:rmi:///jndi/rmi://" + getJmxHostName() + ":" +
				getJmxPort() + "/jmxrmi");

		try (JMXConnector jmxConnector = JMXConnectorFactory.connect(
				jmxServiceURL)) {

			GroovyMBean groovyMBean = new GroovyMBean(
				jmxConnector.getMBeanServerConnection(), getObjectName());

			_methodResult = groovyMBean.invokeMethod(
				getMethodName(), getMethodArgs());
		}
	}

	public void setJmxHostName(Object jmxHostName) {
		_jmxHostName = jmxHostName;
	}

	public void setJmxPort(Object jmxPort) {
		_jmxPort = jmxPort;
	}

	public void setMethodArgs(Object methodArgs) {
		_methodArgs = methodArgs;
	}

	public void setMethodName(Object methodName) {
		_methodName = methodName;
	}

	public void setObjectName(Object objectName) {
		_objectName = objectName;
	}

	private Object _jmxHostName = "localhost";
	private Object _jmxPort = 8099;
	private Object _methodArgs;
	private Object _methodName;
	private Object _methodResult;
	private Object _objectName;

}