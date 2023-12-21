/*
 * ******************************************************************************
 *  * Copyright (c) 2022. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */

package net.entframework.build.mavenplugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/**
 * A {@link Task} to document the plugin's goals.
 *
 * @author Andy Wilkinson
 */
public class DocumentPluginGoals extends DefaultTask {

	private final PluginXmlParser parser = new PluginXmlParser();

	private File pluginXml;

	private File outputDir;

	private Map<String, String> goalSections;

	@OutputDirectory
	public File getOutputDir() {
		return this.outputDir;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	@Input
	public Map<String, String> getGoalSections() {
		return this.goalSections;
	}

	public void setGoalSections(Map<String, String> goalSections) {
		this.goalSections = goalSections;
	}

	@InputFile
	public File getPluginXml() {
		return this.pluginXml;
	}

	public void setPluginXml(File pluginXml) {
		this.pluginXml = pluginXml;
	}

	@TaskAction
	public void documentPluginGoals() throws IOException {
		PluginXmlParser.Plugin plugin = this.parser.parse(this.pluginXml);
		writeOverview(plugin);
		for (PluginXmlParser.Mojo mojo : plugin.getMojos()) {
			documentMojo(plugin, mojo);
		}
	}

	private void writeOverview(PluginXmlParser.Plugin plugin) throws IOException {
		try (PrintWriter writer = new PrintWriter(new FileWriter(new File(this.outputDir, "overview.adoc")))) {
			writer.println("[cols=\"1,3\"]");
			writer.println("|===");
			writer.println("| Goal | Description");
			writer.println();
			for (PluginXmlParser.Mojo mojo : plugin.getMojos()) {
				writer.printf("| <<%s,%s:%s>>%n", goalSectionId(mojo), plugin.getGoalPrefix(), mojo.getGoal());
				writer.printf("| %s%n", mojo.getDescription());
				writer.println();
			}
			writer.println("|===");
		}
	}

	private void documentMojo(PluginXmlParser.Plugin plugin, PluginXmlParser.Mojo mojo) throws IOException {
		try (PrintWriter writer = new PrintWriter(new FileWriter(new File(this.outputDir, mojo.getGoal() + ".adoc")))) {
			String sectionId = goalSectionId(mojo);
			writer.println();
			writer.println();
			writer.printf("[[%s]]%n", sectionId);
			writer.printf("= `%s:%s`%n", plugin.getGoalPrefix(), mojo.getGoal());
			writer.printf("`%s:%s:%s`%n", plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion());
			writer.println();
			writer.println(mojo.getDescription());
			List<PluginXmlParser.Parameter> parameters = mojo.getParameters().stream().filter(PluginXmlParser.Parameter::isEditable).toList();
			List<PluginXmlParser.Parameter> requiredParameters = parameters.stream().filter(PluginXmlParser.Parameter::isRequired).toList();
			String detailsSectionId = sectionId + ".parameter-details";
			if (!requiredParameters.isEmpty()) {
				writer.println();
				writer.println();
				writer.printf("[[%s.required-parameters]]%n", sectionId);
				writer.println("== Required parameters");
				writeParametersTable(writer, detailsSectionId, requiredParameters);
			}
			List<PluginXmlParser.Parameter> optionalParameters = parameters.stream()
					.filter((parameter) -> !parameter.isRequired())
					.toList();
			if (!optionalParameters.isEmpty()) {
				writer.println();
				writer.println();
				writer.printf("[[%s.optional-parameters]]%n", sectionId);
				writer.println("== Optional parameters");
				writeParametersTable(writer, detailsSectionId, optionalParameters);
			}
			writer.println();
			writer.println();
			writer.printf("[[%s]]%n", detailsSectionId);
			writer.println("== Parameter details");
			writeParameterDetails(writer, parameters, detailsSectionId);
		}
	}

	private String goalSectionId(PluginXmlParser.Mojo mojo) {
		String goalSection = this.goalSections.get(mojo.getGoal());
		if (goalSection == null) {
			throw new IllegalStateException("Goal '" + mojo.getGoal() + "' has not be assigned to a section");
		}
		String sectionId = goalSection + "." + mojo.getGoal() + "-goal";
		return sectionId;
	}

	private void writeParametersTable(PrintWriter writer, String detailsSectionId, List<PluginXmlParser.Parameter> parameters) {
		writer.println("[cols=\"3,2,3\"]");
		writer.println("|===");
		writer.println("| Name | Type | Default");
		writer.println();
		for (PluginXmlParser.Parameter parameter : parameters) {
			String name = parameter.getName();
			writer.printf("| <<%s.%s,%s>>%n", detailsSectionId, parameterId(name), name);
			writer.printf("| `%s`%n", typeNameToJavadocLink(shortTypeName(parameter.getType()), parameter.getType()));
			String defaultValue = parameter.getDefaultValue();
			if (defaultValue != null) {
				writer.printf("| `%s`%n", defaultValue);
			}
			else {
				writer.println("|");
			}
			writer.println();
		}
		writer.println("|===");
	}

	private void writeParameterDetails(PrintWriter writer, List<PluginXmlParser.Parameter> parameters, String sectionId) {
		for (PluginXmlParser.Parameter parameter : parameters) {
			String name = parameter.getName();
			writer.println();
			writer.println();
			writer.printf("[[%s.%s]]%n", sectionId, parameterId(name));
			writer.printf("=== `%s`%n", name);
			writer.println(parameter.getDescription());
			writer.println();
			writer.println("[cols=\"10h,90\"]");
			writer.println("|===");
			writer.println();
			writeDetail(writer, "Name", name);
			writeDetail(writer, "Type", typeNameToJavadocLink(parameter.getType()));
			writeOptionalDetail(writer, "Default value", parameter.getDefaultValue());
			writeOptionalDetail(writer, "User property", parameter.getUserProperty());
			writeOptionalDetail(writer, "Since", parameter.getSince());
			writer.println("|===");
		}
	}

	private String parameterId(String name) {
		StringBuilder id = new StringBuilder(name.length() + 4);
		for (char c : name.toCharArray()) {
			if (Character.isLowerCase(c)) {
				id.append(c);
			}
			else {
				id.append("-");
				id.append(Character.toLowerCase(c));
			}
		}
		return id.toString();
	}

	private void writeDetail(PrintWriter writer, String name, String value) {
		writer.printf("| %s%n", name);
		writer.printf("| `%s`%n", value);
		writer.println();
	}

	private void writeOptionalDetail(PrintWriter writer, String name, String value) {
		writer.printf("| %s%n", name);
		if (value != null) {
			writer.printf("| `%s`%n", value);
		}
		else {
			writer.println("|");
		}
		writer.println();
	}

	private String shortTypeName(String name) {
		if (name.lastIndexOf('.') >= 0) {
			name = name.substring(name.lastIndexOf('.') + 1);
		}
		if (name.lastIndexOf('$') >= 0) {
			name = name.substring(name.lastIndexOf('$') + 1);
		}
		return name;
	}

	private String typeNameToJavadocLink(String name) {
		return typeNameToJavadocLink(name, name);
	}

	private String typeNameToJavadocLink(String shortName, String name) {
		if (name.startsWith("org.springframework.boot.maven")) {
			return "{spring-boot-docs}/maven-plugin/api/" + typeNameToJavadocPath(name) + ".html[" + shortName + "]";
		}
		if (name.startsWith("org.springframework.boot")) {
			return "{spring-boot-docs}/api/" + typeNameToJavadocPath(name) + ".html[" + shortName + "]";
		}
		return shortName;
	}

	private String typeNameToJavadocPath(String name) {
		return name.replace(".", "/").replace("$", ".");
	}

}
