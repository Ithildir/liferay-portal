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

package com.liferay.gradle.plugins.notifier.internal;

import com.liferay.gradle.plugins.notifier.Notifier;

import org.gradle.BuildAdapter;
import org.gradle.BuildResult;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.invocation.Gradle;

/**
 * @author Andrea Di Giorgi
 */
public class NotifierBuildListener extends BuildAdapter {

	@Override
	public void buildFinished(BuildResult buildResult) {
		Throwable t = buildResult.getFailure();

		if (t == null) {
			return;
		}

		Gradle gradle = buildResult.getGradle();

		TaskExecutionGraph taskExecutionGraph = gradle.getTaskGraph();

		for (Task task : taskExecutionGraph.getAllTasks()) {
			_notifier.send(task, t);
		}
	}

	public NotifierBuildListener(Notifier notifier) {
		_notifier = notifier;
	}

	private final Notifier _notifier;

}