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

import com.liferay.gradle.plugins.notifier.internal.NamedNotifier;

import org.gradle.util.ConfigureUtil;

import groovy.lang.Closure;

/**
 * @author Andrea Di Giorgi
 */
public abstract class BaseNotifierImpl implements NamedNotifier {

	@Override
	@SuppressWarnings("rawtypes")
	public NamedNotifier configure(Closure closure) {
		return ConfigureUtil.configure(closure, this);
	}

}
