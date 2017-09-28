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

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.JavaExec;

/**
 * @author David Truong
 */
public class UnforkedJavaExec extends JavaExec {

	@Override
	public void exec() {
		List<String> args = getArgs();
		FileCollection classpath = getClasspath();
		String mainClassName = getMain();

		PrintStream originalErrorStream = System.err;
		InputStream originalInputStream = System.in;
		PrintStream originalOutputStream = System.out;

		Thread currentThread = Thread.currentThread();

		ClassLoader originalContextClassLoader =
			currentThread.getContextClassLoader();

		String originalClasspath = System.getProperty("java.class.path");

		try {
			ClassLoader classLoader = _createClassLoader(classpath);

			currentThread.setContextClassLoader(classLoader);

			System.setProperty("java.class.path", classpath.getAsPath());

			Class<?> clazz = classLoader.loadClass(mainClassName);

			Method mainMethod = clazz.getDeclaredMethod("main", String[].class);

			String[] mainArgs = args.toArray(new String[args.size()]);

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

			mainMethod.invoke(null, (Object)mainArgs);
		}
		catch (Exception e) {
			if (isIgnoreExitValue()) {
				_logger.error("Unable to execute class '{}'", e, mainClassName);
			}
			else {
				throw new GradleException(
					"Unable to execute class '" + mainClassName + "'", e);
			}
		}
		finally {
			System.setErr(originalErrorStream);
			System.setIn(originalInputStream);
			System.setOut(originalOutputStream);

			currentThread.setContextClassLoader(originalContextClassLoader);

			System.setProperty("java.class.path", originalClasspath);
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

		return URLClassLoader.newInstance(
			urls.toArray(new URL[urls.size()]), null);
	}

	private static final Logger _logger = Logging.getLogger(
		UnforkedJavaExec.class);

}