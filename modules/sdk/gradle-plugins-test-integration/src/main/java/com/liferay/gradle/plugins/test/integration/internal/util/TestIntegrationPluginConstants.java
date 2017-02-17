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

/**
 * @author Andrea Di Giorgi
 */
public interface TestIntegrationPluginConstants {

	public static final long DEFAULT_CHECK_INTERVAL = 500;

	public static final String DEFAULT_CHECK_PATH = "/web/guest";

	public static final long DEFAULT_CHECK_TIMEOUT = 5 * 60 * 1000;

	public static final int DEFAULT_PORT_HTTP = 8080;

	public static final int DEFAULT_PORT_JMX = 8099;

	public static final int DEFAULT_PORT_JWDP = 5005;

}