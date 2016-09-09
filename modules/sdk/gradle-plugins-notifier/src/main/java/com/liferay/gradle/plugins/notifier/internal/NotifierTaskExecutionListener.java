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

import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionAdapter;
import org.gradle.api.tasks.TaskState;

/**
 * @author Andrea Di Giorgi
 */
public class NotifierTaskExecutionListener extends TaskExecutionAdapter {

	public void afterExecute(Task task, TaskState taskState) {
		if (taskState.getDidWork()) {
			_notifier.send(task, taskState.getFailure());
		}
	};

	private final Notifier _notifier;

	public NotifierTaskExecutionListener(Notifier notifier) {
		_notifier = notifier;
	}

}