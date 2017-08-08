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

package com.liferay.gradle.plugins.internal.util;

import java.io.File;

import java.util.Iterator;
import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.BasePluginConvention;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.TaskContainer;

/**
 * @author Andrea Di Giorgi
 */
public class GradleUtil extends com.liferay.gradle.util.GradleUtil {

	public static String getArchivesBaseName(Project project) {
		BasePluginConvention basePluginConvention = getConvention(
			project, BasePluginConvention.class);

		return basePluginConvention.getArchivesBaseName();
	}

	public static <T> T getConvention(Task task, Class<T> clazz) {
		Convention convention = task.getConvention();

		return convention.getPlugin(clazz);
	}

	public static File getSrcDir(SourceDirectorySet sourceDirectorySet) {
		Set<File> srcDirs = sourceDirectorySet.getSrcDirs();

		Iterator<File> iterator = srcDirs.iterator();

		return iterator.next();
	}

	public static boolean hasPlugin(
		Project project, Class<? extends Plugin<?>> pluginClass) {

		PluginContainer pluginContainer = project.getPlugins();

		return pluginContainer.hasPlugin(pluginClass);
	}

	public static boolean hasPlugin(Project project, String pluginId) {
		PluginContainer pluginContainer = project.getPlugins();

		return pluginContainer.hasPlugin(pluginId);
	}

	public static boolean hasTask(Project project, String name) {
		TaskContainer taskContainer = project.getTasks();

		if (taskContainer.findByName(name) != null) {
			return true;
		}

		return false;
	}

	public static <P extends Plugin<? extends Project>> void withPlugin(
		Project project, Class<P> pluginClass, Action<P> action) {

		PluginContainer pluginContainer = project.getPlugins();

		pluginContainer.withType(pluginClass, action);
	}

}