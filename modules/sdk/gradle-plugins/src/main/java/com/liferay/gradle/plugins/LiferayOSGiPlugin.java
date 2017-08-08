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

package com.liferay.gradle.plugins;

import aQute.bnd.gradle.BndBuilderPlugin;
import aQute.bnd.gradle.Bundle;
import aQute.bnd.gradle.BundleTaskConvention;
import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Constants;

import com.liferay.gradle.plugins.css.builder.CSSBuilderPlugin;
import com.liferay.gradle.plugins.extensions.LiferayExtension;
import com.liferay.gradle.plugins.extensions.LiferayOSGiExtension;
import com.liferay.gradle.plugins.internal.AlloyTaglibDefaultsPlugin;
import com.liferay.gradle.plugins.internal.CSSBuilderDefaultsPlugin;
import com.liferay.gradle.plugins.internal.DBSupportDefaultsPlugin;
import com.liferay.gradle.plugins.internal.EclipseDefaultsPlugin;
import com.liferay.gradle.plugins.internal.FindBugsDefaultsPlugin;
import com.liferay.gradle.plugins.internal.IdeaDefaultsPlugin;
import com.liferay.gradle.plugins.internal.JSModuleConfigGeneratorDefaultsPlugin;
import com.liferay.gradle.plugins.internal.JavadocFormatterDefaultsPlugin;
import com.liferay.gradle.plugins.internal.JspCDefaultsPlugin;
import com.liferay.gradle.plugins.internal.ServiceBuilderDefaultsPlugin;
import com.liferay.gradle.plugins.internal.TLDFormatterDefaultsPlugin;
import com.liferay.gradle.plugins.internal.TestIntegrationDefaultsPlugin;
import com.liferay.gradle.plugins.internal.UpgradeTableBuilderDefaultsPlugin;
import com.liferay.gradle.plugins.internal.WSDDBuilderDefaultsPlugin;
import com.liferay.gradle.plugins.internal.XMLFormatterDefaultsPlugin;
import com.liferay.gradle.plugins.internal.util.FileUtil;
import com.liferay.gradle.plugins.internal.util.GradleUtil;
import com.liferay.gradle.plugins.jasper.jspc.JspCPlugin;
import com.liferay.gradle.plugins.javadoc.formatter.JavadocFormatterPlugin;
import com.liferay.gradle.plugins.js.module.config.generator.JSModuleConfigGeneratorPlugin;
import com.liferay.gradle.plugins.js.transpiler.JSTranspilerPlugin;
import com.liferay.gradle.plugins.lang.builder.LangBuilderPlugin;
import com.liferay.gradle.plugins.node.NodePlugin;
import com.liferay.gradle.plugins.node.tasks.DownloadNodeModuleTask;
import com.liferay.gradle.plugins.node.tasks.NpmInstallTask;
import com.liferay.gradle.plugins.source.formatter.SourceFormatterPlugin;
import com.liferay.gradle.plugins.soy.SoyPlugin;
import com.liferay.gradle.plugins.soy.SoyTranslationPlugin;
import com.liferay.gradle.plugins.soy.tasks.BuildSoyTask;
import com.liferay.gradle.plugins.tasks.DirectDeployTask;
import com.liferay.gradle.plugins.test.integration.TestIntegrationPlugin;
import com.liferay.gradle.plugins.tld.formatter.TLDFormatterPlugin;
import com.liferay.gradle.plugins.tlddoc.builder.TLDDocBuilderPlugin;
import com.liferay.gradle.plugins.wsdd.builder.BuildWSDDTask;
import com.liferay.gradle.plugins.wsdd.builder.WSDDBuilderPlugin;
import com.liferay.gradle.plugins.wsdl.builder.WSDLBuilderPlugin;
import com.liferay.gradle.plugins.xml.formatter.XMLFormatterPlugin;
import com.liferay.gradle.util.StringUtil;
import com.liferay.gradle.util.Validator;

import groovy.lang.Closure;

import java.io.File;

import java.nio.charset.StandardCharsets;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.ApplicationPluginConvention;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.BasePluginConvention;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskInputs;
import org.gradle.api.tasks.TaskOutputs;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.War;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;

/**
 * @author Andrea Di Giorgi
 */
public class LiferayOSGiPlugin implements Plugin<Project> {

	public static final String AUTO_CLEAN_PROPERTY_NAME = "autoClean";

	public static final String AUTO_UPDATE_XML_TASK_NAME = "autoUpdateXml";

	public static final String CLEAN_DEPLOYED_PROPERTY_NAME = "cleanDeployed";

	public static final String COMPILE_INCLUDE_CONFIGURATION_NAME =
		"compileInclude";

	public static final String PLUGIN_NAME = "liferayOSGi";

	@Override
	public void apply(final Project project) {
		GradleUtil.applyPlugin(project, LiferayBasePlugin.class);

		LiferayExtension liferayExtension = GradleUtil.getExtension(
			project, LiferayExtension.class);

		_applyPlugins(project);

		final Jar jar = (Jar)GradleUtil.getTask(
			project, JavaPlugin.JAR_TASK_NAME);

		final LiferayOSGiExtension liferayOSGiExtension =
			GradleUtil.addExtension(
				project, PLUGIN_NAME, LiferayOSGiExtension.class);

		_addDeployedFile(
			project, liferayExtension, JavaPlugin.JAR_TASK_NAME, false);

		final Configuration compileIncludeConfiguration =
			_addConfigurationCompileInclude(project);

		_addTaskAutoUpdateXml(jar);
		_addTasksBuildWSDDJar(project, liferayExtension, liferayOSGiExtension);

		_configureArchivesBaseName(project, liferayOSGiExtension);
		_configureDescription(project, liferayOSGiExtension);
		_configureLiferay(project, liferayExtension);
		_configureSourceSetMain(project);
		_configureTaskClean(project);
		_configureTaskJar(
			jar, liferayOSGiExtension, compileIncludeConfiguration);
		_configureTaskJavadoc(project, liferayOSGiExtension);
		_configureTaskTest(project);
		_configureTasksTest(project);
		_configureVersion(project, liferayOSGiExtension);

		GradleUtil.withPlugin(
			project, ApplicationPlugin.class,
			new Action<ApplicationPlugin>() {

				@Override
				public void execute(ApplicationPlugin applicationPlugin) {
					_configureApplication(project, liferayOSGiExtension);
					_configureTaskRun(project, compileIncludeConfiguration);
				}

			});
	}

	private Configuration _addConfigurationCompileInclude(Project project) {
		Configuration configuration = GradleUtil.addConfiguration(
			project, COMPILE_INCLUDE_CONFIGURATION_NAME);

		configuration.setDescription(
			"Additional dependencies to include in the final JAR.");
		configuration.setVisible(false);

		Configuration compileOnlyConfiguration = GradleUtil.getConfiguration(
			project, JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME);

		compileOnlyConfiguration.extendsFrom(configuration);

		return configuration;
	}

	private void _addDeployedFile(
		final LiferayExtension liferayExtension,
		final AbstractArchiveTask abstractArchiveTask, boolean lazy) {

		final Project project = abstractArchiveTask.getProject();

		Task task = GradleUtil.getTask(
			project, LiferayBasePlugin.DEPLOY_TASK_NAME);

		if (!(task instanceof Copy)) {
			return;
		}

		final Copy copy = (Copy)task;

		Object sourcePath = abstractArchiveTask;

		if (lazy) {
			sourcePath = new Callable<File>() {

				@Override
				public File call() throws Exception {
					return abstractArchiveTask.getArchivePath();
				}

			};
		}

		copy.from(
			sourcePath,
			new Closure<Void>(project) {

				@SuppressWarnings("unused")
				public void doCall(CopySpec copySpec) {
					copySpec.rename(
						new Closure<String>(project) {

							public String doCall(String fileName) {
								Closure<String> deployedFileNameClosure =
									liferayExtension.
										getDeployedFileNameClosure();

								return deployedFileNameClosure.call(
									abstractArchiveTask);
							}

						});
				}

			});

		Delete delete = (Delete)GradleUtil.getTask(
			project, BasePlugin.CLEAN_TASK_NAME);

		if (GradleUtil.getProperty(
				delete, CLEAN_DEPLOYED_PROPERTY_NAME, true)) {

			delete.delete(
				new Callable<File>() {

					@Override
					public File call() throws Exception {
						Closure<String> deployedFileNameClosure =
							liferayExtension.getDeployedFileNameClosure();

						return new File(
							copy.getDestinationDir(),
							deployedFileNameClosure.call(abstractArchiveTask));
					}

				});
		}
	}

	private void _addDeployedFile(
		Project project, LiferayExtension liferayExtension, String taskName,
		boolean lazy) {

		AbstractArchiveTask abstractArchiveTask =
			(AbstractArchiveTask)GradleUtil.getTask(project, taskName);

		_addDeployedFile(liferayExtension, abstractArchiveTask, lazy);
	}

	private DirectDeployTask _addTaskAutoUpdateXml(final Jar jar) {
		final DirectDeployTask directDeployTask = GradleUtil.addTask(
			jar.getProject(), AUTO_UPDATE_XML_TASK_NAME,
			DirectDeployTask.class);

		directDeployTask.setAppServerDeployDir(
			directDeployTask.getTemporaryDir());
		directDeployTask.setAppServerType("tomcat");

		directDeployTask.setWebAppFile(
			new Callable<File>() {

				@Override
				public File call() throws Exception {
					return FileUtil.replaceExtension(
						jar.getArchivePath(), War.WAR_EXTENSION);
				}

			});

		directDeployTask.setWebAppType("portlet");

		directDeployTask.doFirst(
			new Action<Task>() {

				@Override
				public void execute(Task task) {
					DirectDeployTask directDeployTask = (DirectDeployTask)task;

					Jar jar = (Jar)GradleUtil.getTask(
						directDeployTask.getProject(),
						JavaPlugin.JAR_TASK_NAME);

					File jarFile = jar.getArchivePath();

					jarFile.renameTo(directDeployTask.getWebAppFile());
				}

			});

		directDeployTask.doLast(
			new Action<Task>() {

				@Override
				public void execute(Task task) {
					Logger logger = task.getLogger();
					Project project = task.getProject();

					project.delete("liferay/logs");

					File liferayDir = project.file("liferay");

					boolean deleted = liferayDir.delete();

					if (!deleted && logger.isInfoEnabled()) {
						logger.info("Unable to delete " + liferayDir);
					}
				}

			});

		directDeployTask.doLast(
			new Action<Task>() {

				@Override
				public void execute(Task task) {
					DirectDeployTask directDeployTask = (DirectDeployTask)task;

					Project project = directDeployTask.getProject();

					File warFile = directDeployTask.getWebAppFile();

					Jar jar = (Jar)GradleUtil.getTask(
						project, JavaPlugin.JAR_TASK_NAME);

					String deployedPluginDirName = FileUtil.stripExtension(
						jar.getArchiveName());

					File deployedPluginDir = new File(
						directDeployTask.getAppServerDeployDir(),
						deployedPluginDirName);

					if (!deployedPluginDir.exists()) {
						deployedPluginDir = new File(
							directDeployTask.getAppServerDeployDir(),
							project.getName());
					}

					if (!deployedPluginDir.exists()) {
						_logger.warn(
							"Unable to automatically update web.xml in " +
								jar.getArchivePath());

						return;
					}

					FileUtil.touchFiles(
						project, deployedPluginDir, 0,
						"WEB-INF/liferay-web.xml", "WEB-INF/web.xml",
						"WEB-INF/tld/*");

					deployedPluginDirName = project.relativePath(
						deployedPluginDir);

					LiferayExtension liferayExtension = GradleUtil.getExtension(
						project, LiferayExtension.class);

					String[][] filesets = {
						{
							project.relativePath(
								liferayExtension.getAppServerPortalDir()),
							"WEB-INF/tld/c.tld"
						},
						{
							deployedPluginDirName,
							"WEB-INF/liferay-web.xml,WEB-INF/web.xml"
						},
						{deployedPluginDirName, "WEB-INF/tld/*"}
					};

					FileUtil.jar(project, warFile, "preserve", true, filesets);

					warFile.renameTo(jar.getArchivePath());
				}

			});

		directDeployTask.onlyIf(
			new Spec<Task>() {

				@Override
				public boolean isSatisfiedBy(Task task) {
					Project project = task.getProject();

					LiferayOSGiExtension liferayOSGiExtension =
						GradleUtil.getExtension(
							project, LiferayOSGiExtension.class);

					if (liferayOSGiExtension.isAutoUpdateXml() &&
						FileUtil.exists(
							project, "docroot/WEB-INF/portlet.xml")) {

						return true;
					}

					return false;
				}

			});

		TaskInputs taskInputs = directDeployTask.getInputs();

		taskInputs.file(
			new Callable<File>() {

				@Override
				public File call() throws Exception {
					return jar.getArchivePath();
				}

			});

		jar.doLast(
			new Action<Task>() {

				@Override
				public void execute(Task task) {
					directDeployTask.execute();
				}

			});

		return directDeployTask;
	}

	private Bundle _addTaskBuildWSDDJar(
		final BuildWSDDTask buildWSDDTask, LiferayExtension liferayExtension,
		final LiferayOSGiExtension liferayOSGiExtension) {

		Project project = buildWSDDTask.getProject();

		Bundle bundle = GradleUtil.addTask(
			project, buildWSDDTask.getName() + "Jar", Bundle.class);

		bundle.dependsOn(buildWSDDTask);

		bundle.doFirst(
			new Action<Task>() {

				@Override
				public void execute(Task task) {
					Project project = task.getProject();

					Map<String, String> bndProperties = new HashMap<>();

					String bundleName =
						liferayOSGiExtension.getBundleInstruction(
							Constants.BUNDLE_NAME);

					if (Validator.isNotNull(bundleName)) {
						bndProperties.put(
							Constants.BUNDLE_NAME,
							bundleName + " WSDD descriptors");
					}

					String bundleSymbolicName =
						liferayOSGiExtension.getBundleInstruction(
							Constants.BUNDLE_SYMBOLICNAME);

					bndProperties.put(
						Constants.BUNDLE_SYMBOLICNAME,
						bundleSymbolicName + ".wsdd");
					bndProperties.put(
						Constants.FRAGMENT_HOST, bundleSymbolicName);

					bndProperties.put(
						Constants.IMPORT_PACKAGE,
						"javax.servlet,javax.servlet.http");

					StringBuilder sb = new StringBuilder();

					sb.append("WEB-INF/=");
					sb.append(
						FileUtil.getRelativePath(
							project, buildWSDDTask.getServerConfigFile()));
					sb.append(',');
					sb.append(
						FileUtil.getRelativePath(
							project, buildWSDDTask.getOutputDir()));
					sb.append(";filter:=*.wsdd");

					bndProperties.put(
						Constants.INCLUDE_RESOURCE, sb.toString());

					BundleTaskConvention bundleTaskConvention =
						GradleUtil.getConvention(
							task, BundleTaskConvention.class);

					bundleTaskConvention.setBnd(bndProperties);
				}

			});

		String taskName = buildWSDDTask.getName();

		if (taskName.equals(WSDDBuilderPlugin.BUILD_WSDD_TASK_NAME)) {
			bundle.setAppendix("wsdd");
		}
		else {
			bundle.setAppendix("wsdd-" + taskName);
		}

		buildWSDDTask.finalizedBy(bundle);

		_addDeployedFile(liferayExtension, bundle, true);

		return bundle;
	}

	private void _addTasksBuildWSDDJar(
		Project project, final LiferayExtension liferayExtension,
		final LiferayOSGiExtension liferayOSGiExtension) {

		TaskContainer taskContainer = project.getTasks();

		taskContainer.withType(
			BuildWSDDTask.class,
			new Action<BuildWSDDTask>() {

				@Override
				public void execute(BuildWSDDTask buildWSDDTask) {
					_addTaskBuildWSDDJar(
						buildWSDDTask, liferayExtension, liferayOSGiExtension);
				}

			});
	}

	private void _applyPlugins(Project project) {
		GradleUtil.applyPlugin(project, BndBuilderPlugin.class);
		GradleUtil.applyPlugin(project, CSSBuilderPlugin.class);

		GradleUtil.applyPlugin(project, NodePlugin.class);

		if (!GradleUtil.hasTask(project, NodePlugin.NPM_RUN_BUILD_TASK_NAME)) {
			GradleUtil.applyPlugin(
				project, JSModuleConfigGeneratorPlugin.class);
			GradleUtil.applyPlugin(project, JSTranspilerPlugin.class);
		}

		GradleUtil.applyPlugin(project, JavadocFormatterPlugin.class);
		GradleUtil.applyPlugin(project, JspCPlugin.class);
		GradleUtil.applyPlugin(project, LangBuilderPlugin.class);
		GradleUtil.applyPlugin(project, SourceFormatterPlugin.class);
		GradleUtil.applyPlugin(project, SoyPlugin.class);
		GradleUtil.applyPlugin(project, SoyTranslationPlugin.class);
		GradleUtil.applyPlugin(project, TLDDocBuilderPlugin.class);
		GradleUtil.applyPlugin(project, TLDFormatterPlugin.class);
		GradleUtil.applyPlugin(project, TestIntegrationPlugin.class);
		GradleUtil.applyPlugin(project, XMLFormatterPlugin.class);

		AlloyTaglibDefaultsPlugin.INSTANCE.apply(project);
		CSSBuilderDefaultsPlugin.INSTANCE.apply(project);
		DBSupportDefaultsPlugin.INSTANCE.apply(project);
		EclipseDefaultsPlugin.INSTANCE.apply(project);
		FindBugsDefaultsPlugin.INSTANCE.apply(project);
		IdeaDefaultsPlugin.INSTANCE.apply(project);
		JSModuleConfigGeneratorDefaultsPlugin.INSTANCE.apply(project);
		JavadocFormatterDefaultsPlugin.INSTANCE.apply(project);
		JspCDefaultsPlugin.INSTANCE.apply(project);
		ServiceBuilderDefaultsPlugin.INSTANCE.apply(project);
		TLDFormatterDefaultsPlugin.INSTANCE.apply(project);
		TestIntegrationDefaultsPlugin.INSTANCE.apply(project);
		UpgradeTableBuilderDefaultsPlugin.INSTANCE.apply(project);
		WSDDBuilderDefaultsPlugin.INSTANCE.apply(project);
		XMLFormatterDefaultsPlugin.INSTANCE.apply(project);
	}

	private void _configureApplication(
		Project project, LiferayOSGiExtension liferayOSGiExtension) {

		ApplicationPluginConvention applicationPluginConvention =
			GradleUtil.getConvention(
				project, ApplicationPluginConvention.class);

		String mainClassName = liferayOSGiExtension.getBundleInstruction(
			"Main-Class");

		if (Validator.isNotNull(mainClassName)) {
			applicationPluginConvention.setMainClassName(mainClassName);
		}
	}

	private void _configureArchivesBaseName(
		Project project, LiferayOSGiExtension liferayOSGiExtension) {

		BasePluginConvention basePluginConvention = GradleUtil.getConvention(
			project, BasePluginConvention.class);

		String bundleSymbolicName = liferayOSGiExtension.getBundleInstruction(
			Constants.BUNDLE_SYMBOLICNAME);

		if (Validator.isNull(bundleSymbolicName)) {
			return;
		}

		Parameters parameters = new Parameters(bundleSymbolicName);

		Set<String> keys = parameters.keySet();

		Iterator<String> iterator = keys.iterator();

		bundleSymbolicName = iterator.next();

		basePluginConvention.setArchivesBaseName(bundleSymbolicName);
	}

	private void _configureDescription(
		Project project, LiferayOSGiExtension liferayOSGiExtension) {

		String description = liferayOSGiExtension.getBundleInstruction(
			Constants.BUNDLE_DESCRIPTION);

		if (Validator.isNull(description)) {
			description = liferayOSGiExtension.getBundleInstruction(
				Constants.BUNDLE_NAME);
		}

		if (Validator.isNotNull(description)) {
			project.setDescription(description);
		}
	}

	private void _configureLiferay(
		final Project project, final LiferayExtension liferayExtension) {

		liferayExtension.setDeployDir(
			new Callable<File>() {

				@Override
				public File call() throws Exception {
					File dir = new File(
						liferayExtension.getAppServerParentDir(),
						"osgi/modules");

					return GradleUtil.getProperty(
						project, "auto.deploy.dir", dir);
				}

			});
	}

	private void _configureSourceSetMain(Project project) {
		File docrootDir = project.file("docroot");

		if (!docrootDir.exists()) {
			return;
		}

		SourceSet sourceSet = GradleUtil.getSourceSet(
			project, SourceSet.MAIN_SOURCE_SET_NAME);

		SourceSetOutput sourceSetOutput = sourceSet.getOutput();

		File classesDir = new File(docrootDir, "WEB-INF/classes");

		sourceSetOutput.setClassesDir(classesDir);
		sourceSetOutput.setResourcesDir(classesDir);

		SourceDirectorySet javaSourceDirectorySet = sourceSet.getJava();

		File srcDir = new File(docrootDir, "WEB-INF/src");

		Set<File> srcDirs = Collections.singleton(srcDir);

		javaSourceDirectorySet.setSrcDirs(srcDirs);

		SourceDirectorySet resourcesSourceDirectorySet =
			sourceSet.getResources();

		resourcesSourceDirectorySet.setSrcDirs(srcDirs);
	}

	private void _configureTaskClean(Project project) {
		Task task = GradleUtil.getTask(project, BasePlugin.CLEAN_TASK_NAME);

		if (task instanceof Delete) {
			_configureTaskCleanDependsOn((Delete)task);
		}
	}

	private void _configureTaskCleanDependsOn(Delete delete) {
		Project project = delete.getProject();

		Closure<Set<String>> closure = new Closure<Set<String>>(project) {

			@SuppressWarnings("unused")
			public Set<String> doCall(Delete delete) {
				Set<String> cleanTaskNames = new HashSet<>();

				Project project = delete.getProject();

				for (Task task : project.getTasks()) {
					String taskName = task.getName();

					if (taskName.equals(LiferayBasePlugin.DEPLOY_TASK_NAME) ||
						taskName.equals("eclipseClasspath") ||
						taskName.equals("eclipseProject") ||
						taskName.equals("ideaModule") ||
						(task instanceof BuildSoyTask) ||
						(task instanceof DownloadNodeModuleTask) ||
						(task instanceof NpmInstallTask)) {

						continue;
					}

					if (GradleUtil.hasPlugin(project, _CACHE_PLUGIN_ID) &&
						taskName.startsWith("save") &&
						taskName.endsWith("Cache")) {

						continue;
					}

					if (GradleUtil.hasPlugin(
							project, WSDLBuilderPlugin.class) &&
						taskName.startsWith(
							WSDLBuilderPlugin.BUILD_WSDL_TASK_NAME +
								"Generate")) {

						continue;
					}

					boolean autoClean = GradleUtil.getProperty(
						task, AUTO_CLEAN_PROPERTY_NAME, true);

					if (!autoClean) {
						continue;
					}

					TaskOutputs taskOutputs = task.getOutputs();

					if (!taskOutputs.getHasOutput()) {
						continue;
					}

					cleanTaskNames.add(
						BasePlugin.CLEAN_TASK_NAME +
							StringUtil.capitalize(taskName));
				}

				return cleanTaskNames;
			}

		};

		delete.dependsOn(closure);
	}

	private void _configureTaskJar(
		Jar jar, final LiferayOSGiExtension liferayOSGiExtension,
		final Configuration compileIncludeConfiguration) {

		jar.doFirst(
			new Action<Task>() {

				@Override
				public void execute(Task task) {
					Jar jar = (Jar)task;

					BundleTaskConvention bundleTaskConvention =
						GradleUtil.getConvention(
							jar, BundleTaskConvention.class);

					bundleTaskConvention.setBnd(
						_getBundleCompleteInstructions(
							liferayOSGiExtension, compileIncludeConfiguration));

					// Since IllegalArgumentException is thrown if set to null,
					// we have to pass a non-existent file name.

					bundleTaskConvention.setBndfile(
						String.valueOf(System.currentTimeMillis()));
				}

			});
	}

	private void _configureTaskJavadoc(
		Project project, LiferayOSGiExtension liferayOSGiExtension) {

		String bundleName = liferayOSGiExtension.getBundleInstruction(
			Constants.BUNDLE_NAME);
		String bundleVersion = liferayOSGiExtension.getBundleInstruction(
			Constants.BUNDLE_VERSION);

		if (Validator.isNull(bundleName) || Validator.isNull(bundleVersion)) {
			return;
		}

		Javadoc javadoc = (Javadoc)GradleUtil.getTask(
			project, JavaPlugin.JAVADOC_TASK_NAME);

		String title = String.format("%s %s API", bundleName, bundleVersion);

		javadoc.setTitle(title);
	}

	private void _configureTaskRun(
		Project project, Configuration compileIncludeConfiguration) {

		JavaExec javaExec = (JavaExec)GradleUtil.getTask(
			project, ApplicationPlugin.TASK_RUN_NAME);

		javaExec.classpath(compileIncludeConfiguration);
	}

	private void _configureTasksTest(Project project) {
		TaskContainer taskContainer = project.getTasks();

		taskContainer.withType(
			Test.class,
			new Action<Test>() {

				@Override
				public void execute(Test test) {
					_configureTaskTestDefaultCharacterEncoding(test);
				}

			});
	}

	private void _configureTaskTest(Project project) {
		final Test test = (Test)GradleUtil.getTask(
			project, JavaPlugin.TEST_TASK_NAME);

		test.jvmArgs(
			"-Djava.net.preferIPv4Stack=true", "-Dliferay.mode=test",
			"-Duser.timezone=GMT");

		test.setForkEvery(1L);

		project.afterEvaluate(
			new Action<Project>() {

				@Override
				public void execute(Project project) {
					_configureTaskTestIncludes(test);
				}

			});
	}

	private void _configureTaskTestDefaultCharacterEncoding(Test test) {
		test.setDefaultCharacterEncoding(StandardCharsets.UTF_8.name());
	}

	private void _configureTaskTestIncludes(Test test) {
		Set<String> includes = test.getIncludes();

		if (includes.isEmpty()) {
			test.setIncludes(Collections.singleton("**/*Test.class"));
		}
	}

	private void _configureVersion(
		Project project, LiferayOSGiExtension liferayOSGiExtension) {

		String bundleVersion = liferayOSGiExtension.getBundleInstruction(
			Constants.BUNDLE_VERSION);

		if (Validator.isNotNull(bundleVersion)) {
			project.setVersion(bundleVersion);
		}
	}

	private Map<String, Object> _getBundleCompleteInstructions(
		LiferayOSGiExtension liferayOSGiExtension,
		Configuration compileIncludeConfiguration) {

		Map<String, Object> bundleCompleteInstructions = new HashMap<>();

		Map<String, Object> bundleDefaultInstructions =
			liferayOSGiExtension.getBundleDefaultInstructions();

		for (Map.Entry<String, Object> entry :
				bundleDefaultInstructions.entrySet()) {

			bundleCompleteInstructions.put(
				entry.getKey(), GradleUtil.toString(entry.getValue()));
		}

		Properties bundleInstructions =
			liferayOSGiExtension.getBundleInstructions();

		for (String key : bundleInstructions.stringPropertyNames()) {
			bundleCompleteInstructions.put(
				key, bundleInstructions.getProperty(key));
		}

		if (!compileIncludeConfiguration.isEmpty()) {
			boolean expandCompileInclude =
				liferayOSGiExtension.isExpandCompileInclude();

			StringBuilder sb = new StringBuilder();

			for (File file : compileIncludeConfiguration) {
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

			bundleCompleteInstructions.put(
				Constants.INCLUDERESOURCE + "." +
					compileIncludeConfiguration.getName(),
				sb.toString());
		}

		return bundleCompleteInstructions;
	}

	private static final String _CACHE_PLUGIN_ID = "com.liferay.cache";

	private static final Logger _logger = Logging.getLogger(
		LiferayOSGiPlugin.class);

}