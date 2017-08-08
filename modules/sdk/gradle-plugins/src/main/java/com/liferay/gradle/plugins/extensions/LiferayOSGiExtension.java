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

package com.liferay.gradle.plugins.extensions;

import aQute.bnd.osgi.Constants;

import aQute.lib.spring.SpringComponent;

import com.liferay.ant.bnd.jsp.JspAnalyzerPlugin;
import com.liferay.ant.bnd.npm.NpmAnalyzerPlugin;
import com.liferay.ant.bnd.resource.bundle.ResourceBundleLoaderAnalyzerPlugin;
import com.liferay.ant.bnd.sass.SassAnalyzerPlugin;
import com.liferay.ant.bnd.service.ServiceAnalyzerPlugin;
import com.liferay.ant.bnd.social.SocialAnalyzerPlugin;
import com.liferay.ant.bnd.spring.SpringDependencyAnalyzerPlugin;
import com.liferay.gradle.plugins.LiferayOSGiPlugin;
import com.liferay.gradle.plugins.internal.util.FileUtil;
import com.liferay.gradle.plugins.internal.util.GradleUtil;
import com.liferay.gradle.util.StringUtil;
import com.liferay.gradle.util.Validator;

import java.io.File;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.util.GUtil;

/**
 * @author Andrea Di Giorgi
 */
public class LiferayOSGiExtension {

	public static final String DONOTCOPY_DEFAULT = ".*\\.wsdd";

	public LiferayOSGiExtension(Project project) {
		_project = project;

		_bundleInstructions.put(
			Constants.BUNDLE_SYMBOLICNAME, project.getName());
		_bundleInstructions.put(
			Constants.DONOTCOPY, "(" + DONOTCOPY_DEFAULT + ")");
		_bundleInstructions.put(Constants.METATYPE, "*");
		_bundleInstructions.put(
			Constants.PLUGIN, StringUtil.merge(_BND_PLUGIN_CLASS_NAMES, ","));

		_bundleInstructions.put(
			"Javac-Debug",
			new Callable<String>() {

				@Override
				public String call() throws Exception {
					CompileOptions compileOptions = _getCompileOptions();

					return _getOnOffValue(compileOptions.isDebug());
				}

			});

		_bundleInstructions.put(
			"Javac-Deprecation",
			new Callable<String>() {

				@Override
				public String call() throws Exception {
					CompileOptions compileOptions = _getCompileOptions();

					return _getOnOffValue(compileOptions.isDeprecation());
				}

			});

		_bundleInstructions.put(
			"Javac-Encoding",
			new Callable<String>() {

				@Override
				public String call() throws Exception {
					CompileOptions compileOptions = _getCompileOptions();

					String encoding = compileOptions.getEncoding();

					if (Validator.isNull(encoding)) {
						encoding = System.getProperty("file.encoding");
					}

					return encoding;
				}

			});

		_bundleInstructions.put("-jsp", "*.jsp,*.jspf");
		_bundleInstructions.put("-sass", "*");

		_bundleInstructions.put(
			Constants.INCLUDERESOURCE + "." +
				LiferayOSGiPlugin.COMPILE_INCLUDE_CONFIGURATION_NAME,
			new Callable<String>() {

				@Override
				public String call() throws Exception {
					Configuration configuration = GradleUtil.getConfiguration(
						_project,
						LiferayOSGiPlugin.COMPILE_INCLUDE_CONFIGURATION_NAME);

					if (configuration.isEmpty()) {
						return null;
					}

					boolean expandCompileInclude = isExpandCompileInclude();

					StringBuilder sb = new StringBuilder();

					for (File file : configuration) {
						if (sb.length() > 0) {
							sb.append(',');
						}

						if (expandCompileInclude) {
							sb.append('@');
						}
						else {
							sb.append("lib/=");
						}

						sb.append(FileUtil.getAbsolutePath(file));

						if (!expandCompileInclude) {
							sb.append(";lib:=true");
						}
					}

					return sb.toString();
				}

			});

		File bndFile = project.file("bnd.bnd");

		if (bndFile.exists()) {
			Properties properties = GUtil.loadProperties(bndFile);

			for (String key : properties.stringPropertyNames()) {
				_bundleInstructions.put(key, properties.getProperty(key));
			}
		}
	}

	public LiferayOSGiExtension bundleInstruction(String key, Object value) {
		return bundleInstruction(key, value, true);
	}

	public LiferayOSGiExtension bundleInstruction(
		String key, Object value, boolean overwrite) {

		if (overwrite || !_bundleInstructions.containsKey(key)) {
			_bundleInstructions.put(key, value);
		}

		return this;
	}

	public LiferayOSGiExtension bundleInstructions(
		Map<String, ?> bundleInstructions) {

		_bundleInstructions.putAll(bundleInstructions);

		return this;
	}

	public String getBundleInstruction(String key) {
		return GradleUtil.toString(_bundleInstructions.get(key));
	}

	public Map<String, Object> getBundleInstructions() {
		return _bundleInstructions;
	}

	public boolean isAutoUpdateXml() {
		return _autoUpdateXml;
	}

	public boolean isExpandCompileInclude() {
		return _expandCompileInclude;
	}

	public void setAutoUpdateXml(boolean autoUpdateXml) {
		_autoUpdateXml = autoUpdateXml;
	}

	public void setBundleInstructions(Map<String, ?> bundleInstructions) {
		_bundleInstructions.clear();

		bundleInstructions(bundleInstructions);
	}

	public void setExpandCompileInclude(boolean expandCompileInclude) {
		_expandCompileInclude = expandCompileInclude;
	}

	private CompileOptions _getCompileOptions() {
		JavaCompile javaCompile = (JavaCompile)GradleUtil.getTask(
			_project, JavaPlugin.COMPILE_JAVA_TASK_NAME);

		return javaCompile.getOptions();
	}

	private String _getOnOffValue(boolean b) {
		if (b) {
			return "on";
		}

		return "off";
	}

	private static final String[] _BND_PLUGIN_CLASS_NAMES = {
		JspAnalyzerPlugin.class.getName(), NpmAnalyzerPlugin.class.getName(),
		ResourceBundleLoaderAnalyzerPlugin.class.getName(),
		SassAnalyzerPlugin.class.getName(),
		ServiceAnalyzerPlugin.class.getName(),
		SocialAnalyzerPlugin.class.getName(), SpringComponent.class.getName(),
		SpringDependencyAnalyzerPlugin.class.getName()
	};

	private boolean _autoUpdateXml = true;
	private final Map<String, Object> _bundleInstructions = new HashMap<>();
	private boolean _expandCompileInclude;
	private final Project _project;

}