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

package com.liferay.gradle.util.tasks;

import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David Truong
 */
public class UnforkedJavaExec extends JavaExec {

	@TaskAction
	public void exec() {
		PrintStream originalErrStream = System.err;

		InputStream originalInstream = System.in;

		PrintStream originalOutStream = System.out;

		Thread currentThread = Thread.currentThread();

		ClassLoader previousContextClassLoader =
			currentThread.getContextClassLoader();

		String previousClasspath = System.getProperty("java.class.path");

		try {
			FileCollection classpath = getClasspath();

			ClassLoader classLoader = _createClassLoader(classpath);

			currentThread.setContextClassLoader(classLoader);

			System.setProperty("java.class.path", classpath.getAsPath());

			Class<?> c = classLoader.loadClass(getMain());

			Class<?>[] argTypes = {String[].class};

			Method main = c.getDeclaredMethod("main", argTypes);

			String[] mainArgs = getArgs().toArray(new String[0]);

			InputStream inputStream = getStandardInput();

			if (inputStream != null) {
				System.setIn(inputStream);
			}

			OutputStream outputStream = getStandardOutput();

			if (outputStream != null) {
				System.setOut(new PrintStream(outputStream));
			}

			OutputStream errorOutputStream = getErrorOutput();

			if (outputStream != null) {
				System.setErr(new PrintStream(errorOutputStream));
			}

			main.invoke(null, (Object)mainArgs);
		}
		catch (Exception e) {
			_logger.error("Error running method", e);
		}
		finally {
			System.setErr(originalErrStream);

			System.setIn(originalInstream);

			System.setOut(originalOutStream);

			currentThread.setContextClassLoader(previousContextClassLoader);

			System.setProperty("java.class.path", previousClasspath);
		}
	}

	private ClassLoader _createClassLoader(FileCollection classpath)
		throws Exception {

		List<URL> urls = new ArrayList<>();

		for (File file : classpath.getFiles()) {
			if (file.exists()) {
				URI uri = file.toURI();

				urls.add(uri.toURL());
			}
		}

		return URLClassLoader.newInstance(urls.toArray(new URL[0]), null);
	}

	private static final Logger _logger = Logging.getLogger(
		UnforkedJavaExec.class);

}