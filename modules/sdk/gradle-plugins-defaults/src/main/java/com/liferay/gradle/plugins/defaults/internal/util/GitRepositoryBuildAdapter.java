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

package com.liferay.gradle.plugins.defaults.internal.util;

import java.io.File;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.util.FS;

import org.gradle.BuildAdapter;
import org.gradle.BuildResult;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

/**
 * @author Andrea Di Giorgi
 */
public class GitRepositoryBuildAdapter extends BuildAdapter {

	@Override
	public void buildFinished(BuildResult buildResult) {
		Set<Map.Entry<File, Repository>> entries = _repositories.entrySet();

		Iterator<Map.Entry<File, Repository>> iterator = entries.iterator();

		while (iterator.hasNext()) {
			Map.Entry<File, Repository> entry = iterator.next();

			Repository repository = entry.getValue();

			repository.close();

			iterator.remove();
		}
	}

	public Repository getRepository(Project project) {
		return _repositories.computeIfAbsent(
			project.getRootDir(),
			dir -> {
				try {
					File gitDir = _getGitDir(dir);

					return RepositoryCache.open(
						FileKey.exact(gitDir, FS.DETECTED));
				}
				catch (Exception e) {
					throw new GradleException(
						"Unable to get repository for " + project, e);
				}
			});
	}

	private File _getGitDir(File dir) {
		do {
			File gitDir = FileKey.resolve(dir, FS.DETECTED);

			if (gitDir != null) {
				return gitDir;
			}

			dir = dir.getParentFile();
		}
		while (dir != null);

		throw new GradleException("Unable to locate .git directory");
	}

	private final ConcurrentMap<File, Repository> _repositories =
		new ConcurrentHashMap<>();

}