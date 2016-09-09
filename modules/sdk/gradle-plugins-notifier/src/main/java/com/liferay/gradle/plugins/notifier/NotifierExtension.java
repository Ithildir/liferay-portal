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

import com.liferay.gradle.plugins.notifier.internal.NamedNotifier;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.util.ConfigureUtil;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;

/**
 * @author Andrea Di Giorgi
 */
public class NotifierExtension implements Notifier {

	public NotifierExtension(Project project) {
	}

	public void send(Task task, Throwable t) {
		Map<String, NamedNotifier> notifiers = _taskSuccessNotifiers;

		if (t != null) {
			notifiers = _taskFailureNotifiers;
		}

		Notifier notifier = notifiers.get(task.getPath());

		if (notifier != null) {
			notifier.send(task, t);
		}

		notifier = notifiers.get(task.getName());

		if (notifier != null) {
			notifier.send(task, t);
		}
	}

	public void methodMissing(String name, Object[] args) {
		Closure<?> closure = null;
		Notifier notifier = _getNamedNotifier(name);

		if ((args.length == 1) && (args[0] instanceof Closure<?>)) {
			closure = (Closure<?>)args[0];
		}

		if ((closure != null) && (notifier != null)) {
			ConfigureUtil.configure(closure, notifier);
		}
		else {
			throw new MissingMethodException(name, getClass(), args);
		}
	}

	private NamedNotifier _getNamedNotifier(String name) {
		for (NamedNotifier namedNotifier : _namedNotifiers) {
			if (name.equals(namedNotifier.getName())) {
				return namedNotifier;
			}
		}

		return null;
	}

	public void taskSuccess(String task, String name) {
		_addTaskNotifier(_taskSuccessNotifiers, task, name);
	}

	public void taskFailure(String task, String name) {
		_addTaskNotifier(_taskFailureNotifiers, task, name);
	}

	private void _addTaskNotifier(
		Map<String, NamedNotifier> map, String task, String name) {

		NamedNotifier notifier = _getNamedNotifier(name);

		if (notifier == null) {
			throw new IllegalArgumentException(
				"Unable to find notifier '" + name + "'");
		}

		map.put(task, notifier);
	}

	private final Map<String, NamedNotifier> _taskSuccessNotifiers =
		new ConcurrentHashMap<>();

	private final Map<String, NamedNotifier> _taskFailureNotifiers =
		new ConcurrentHashMap<>();

	private final Iterable<NamedNotifier> _namedNotifiers = ServiceLoader.load(
		NamedNotifier.class);

}