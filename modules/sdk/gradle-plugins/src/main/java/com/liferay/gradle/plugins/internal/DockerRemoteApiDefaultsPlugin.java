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

package com.liferay.gradle.plugins.internal;

import com.bmuschko.gradle.docker.DockerExtension;
import com.bmuschko.gradle.docker.DockerRegistryCredentials;
import com.bmuschko.gradle.docker.DockerRemoteApiPlugin;

import com.liferay.gradle.plugins.BaseDefaultsPlugin;
import com.liferay.gradle.plugins.db.support.internal.util.GradleUtil;
import com.liferay.gradle.util.Validator;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Andrea Di Giorgi
 */
public class DockerRemoteApiDefaultsPlugin
	extends BaseDefaultsPlugin<DockerRemoteApiPlugin> {

	public static final Plugin<Project> INSTANCE = new DockerRemoteApiPlugin();

	@Override
	protected void configureDefaults(
		Project project, DockerRemoteApiPlugin dockerRemoteApiPlugin) {

		_configureDocker(project);
	}

	@Override
	protected Class<DockerRemoteApiPlugin> getPluginClass() {
		return DockerRemoteApiPlugin.class;
	}

	private DockerRemoteApiDefaultsPlugin() {
	}

	private void _configureDocker(Project project) {
		DockerExtension dockerExtension = GradleUtil.getExtension(
			project, DockerExtension.class);

		DockerRegistryCredentials dockerRegistryCredentials =
			dockerExtension.getRegistryCredentials();

		String email = GradleUtil.getProperty(
			project, "docker.registry.email", (String)null);

		if (Validator.isNotNull(email)) {
			dockerRegistryCredentials.setEmail(email);
		}

		String password = GradleUtil.getProperty(
			project, "docker.registry.password", (String)null);

		if (Validator.isNotNull(password)) {
			dockerRegistryCredentials.setPassword(password);
		}

		String username = GradleUtil.getProperty(
			project, "docker.registry.username", (String)null);

		if (Validator.isNotNull(username)) {
			dockerRegistryCredentials.setUsername(username);
		}
	}

}