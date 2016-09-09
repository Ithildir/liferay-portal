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

package com.liferay.gradle.plugins.notifier.internal.impl;

import org.gradle.api.Task;

/**
 * @author Andrea Di Giorgi
 */
public class DummyNotifierImpl extends BaseNotifierImpl {

	@Override
	public String getName() {
		return "dummy";
	}

	@Override
	public void send(Task task, Throwable t) {
		if (t != null) {
			System.out.println(
				task + " failed because of the following error: " +
					t.getMessage());
		}
		else {
			System.out.println(task + " completed succesfully");
		}
	}

}
