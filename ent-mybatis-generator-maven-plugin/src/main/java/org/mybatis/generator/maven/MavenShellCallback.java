/*
 * ******************************************************************************
 *  * Copyright (c) 2023. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */
package org.mybatis.generator.maven;

import java.io.File;
import java.nio.file.Path;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.messages.Messages;

/**
 * Shell callback that calculates the Maven output directory.
 *
 * @author Jeff Butler
 */
public class MavenShellCallback extends DefaultShellCallback {

	private MyBatisGeneratorMojo mybatisGeneratorMojo;

	public MavenShellCallback(MyBatisGeneratorMojo mybatisGeneratorMojo, boolean overwrite) {
		super(overwrite);
		this.mybatisGeneratorMojo = mybatisGeneratorMojo;
	}

	@Override
	public File getDirectory(GeneratedFile file) throws ShellException {
		if (StringUtils.isNotBlank(file.getOutputDirectory())) {
			Path path = Path.of(file.getOutputDirectory());
			if (path.isAbsolute()) {
				return getDirectory(path.toAbsolutePath().toString(), file.getTargetProject(), file.getTargetPackage());
			}
			else {
				path = Path.of(mybatisGeneratorMojo.getOutputDirectory().getAbsolutePath(), file.getOutputDirectory());
				return getDirectory(path.toAbsolutePath().toString(), file.getTargetProject(), file.getTargetPackage());
			}
		}
		return getDirectory(mybatisGeneratorMojo.getOutputDirectory().getAbsolutePath(), file.getTargetProject(),
				file.getTargetPackage());
	}

	private File getDirectory(String outputDirectory, String targetProject, String targetPackage)
			throws ShellException {
		File project = new File(outputDirectory);
		if (!project.exists()) {
			project.mkdirs();
		}

		if (!project.isDirectory()) {
			throw new ShellException(Messages.getString("Warning.9", //$NON-NLS-1$
					project.getAbsolutePath()));
		}

		StringBuilder sb = new StringBuilder();

		if ("MAVEN".equals(targetProject)) {
			sb.append("src/main/java/");
		}
		else {
			sb.append(targetProject);
			if (!StringUtils.endsWith(targetProject, "/")) {
				sb.append("/");
			}
		}

		StringTokenizer st = new StringTokenizer(targetPackage, "."); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			sb.append(st.nextToken());
			sb.append(File.separatorChar);
		}

		File directory = new File(project, sb.toString());
		if (!directory.isDirectory()) {
			boolean rc = directory.mkdirs();
			if (!rc) {
				throw new ShellException(Messages.getString("Warning.10", //$NON-NLS-1$
						directory.getAbsolutePath()));
			}
		}

		return directory;
	}

}
