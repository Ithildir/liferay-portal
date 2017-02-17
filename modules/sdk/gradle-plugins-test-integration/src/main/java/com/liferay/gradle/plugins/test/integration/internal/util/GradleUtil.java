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

package com.liferay.gradle.plugins.test.integration.internal.util;

import java.io.File;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.concurrent.Callable;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginContainer;

/**
 * @author Andrea Di Giorgi
 */
public class GradleUtil extends com.liferay.gradle.util.GradleUtil {

	public static boolean hasPlugin(
		Project project, Class<? extends Plugin<?>> pluginClass) {

		PluginContainer pluginContainer = project.getPlugins();

		return pluginContainer.hasPlugin(pluginClass);
	}

	public static boolean isReachable(
		String protocol, String host, int port, String path) {

		try {
			URL url = new URL(protocol, host, port, path);

			HttpURLConnection httpURLConnection =
				(HttpURLConnection)url.openConnection();

			httpURLConnection.setRequestMethod("GET");

			int responseCode = httpURLConnection.getResponseCode();

			if ((responseCode > 0) && (responseCode < 400)) {
				return true;
			}
		}
		catch (IOException ioe) {
		}

		return false;
	}

	public static File toFile(Project project, Object object) {
		object = toObject(object);

		if (object == null) {
			return null;
		}

		return project.file(object);
	}

	public static Long toLong(Object object) {
		object = toObject(object);

		if (object instanceof Long) {
			return (Long)object;
		}

		if (object instanceof Number) {
			Number number = (Number)object;

			return number.longValue();
		}

		if (object instanceof String) {
			return Long.parseLong((String)object);
		}

		return null;
	}

	public static void waitFor(
		Callable<Boolean> callable, long checkInterval, long timeout,
		String messageSuffix) {

		boolean success = false;

		try {
			success = waitFor(callable, checkInterval, timeout);
		}
		catch (Exception e) {
			throw new GradleException(
				"Unable to wait for the " + messageSuffix, e);
		}

		if (!success) {
			throw new GradleException(
				"Timeout while waiting for the " + messageSuffix);
		}
	}

}