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

package com.liferay.gradle.plugins.notifier;

import com.liferay.gradle.plugins.notifier.internal.NotifierBuildListener;
import com.liferay.gradle.plugins.notifier.internal.NotifierTaskExecutionListener;
import com.liferay.gradle.util.GradleUtil;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;

/**
 * @author Andrea Di Giorgi
 */
public class NotifierPlugin implements Plugin<Project> {

	public static final String PLUGIN_NAME = "notifier";

	@Override
	public void apply(Project project) {
		if (project.getParent() != null) {
			throw new GradleException(
				"This plugin can only be applied on the root project");
		}

		NotifierExtension notifierExtension = GradleUtil.addExtension(
			project, PLUGIN_NAME, NotifierExtension.class);

		Gradle gradle = project.getGradle();

		gradle.addListener(new NotifierBuildListener(notifierExtension));
		gradle.addListener(
			new NotifierTaskExecutionListener(notifierExtension));
	}

}