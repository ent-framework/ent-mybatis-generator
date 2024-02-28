/*
 * ******************************************************************************
 *  * Copyright (c) 2022. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */
package net.entframework.kernel.db.generator.typescript;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import net.entframework.kernel.db.generator.plugin.generator.RestMethod;
import net.entframework.kernel.db.generator.plugin.generator.RestMethodAndImports;
import net.entframework.kernel.db.generator.plugin.generator.WebRestMethodsGenerator;
import net.entframework.kernel.db.generator.typescript.render.RenderingUtilities;
import net.entframework.kernel.db.generator.typescript.runtime.FullyQualifiedTypescriptType;
import net.entframework.kernel.db.generator.typescript.runtime.TypescriptTopLevelClass;
import net.entframework.kernel.db.generator.typescript.runtime.Variable;
import net.entframework.kernel.db.generator.utils.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class TypescriptClientGenerator extends AbstractJavaClientGenerator {

	protected String projectRootAlias = "";

	protected String apiDefaultPrefix = "";

	protected String apiEnvName = "";

	public TypescriptClientGenerator(String project) {
		super(project, false);
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		progressCallback.startTask(getString("Progress.17", introspectedTable.getFullyQualifiedTable().toString()));
		preCalculate();

		Interface interfaze = createBasicInterface();

		List<CompilationUnit> answer = new ArrayList<>();

		answer.add(generateRestApiClass());
		return answer;
	}

	protected void preCalculate() {

		Properties properties = this.context.getJavaClientGeneratorConfiguration().getProperties();

		projectRootAlias = this.getProperty("projectRootAlias", properties, this.context.getProperties());
		if (StringUtils.isBlank(projectRootAlias)) {
			projectRootAlias = "";
		}
		// apiDefaultPrefix = this.getProperty("apiDefaultPrefix", properties,
		// this.context.getProperties());
		// if (StringUtils.isBlank(apiDefaultPrefix)) {
		// apiDefaultPrefix = "";
		// }
		// apiEnvName = this.getProperty("apiEnvName", properties,
		// this.context.getProperties());
		// if (StringUtils.isBlank(apiEnvName)) {
		// apiEnvName = "";
		// }
		apiDefaultPrefix = this.context.getJavaClientGeneratorConfiguration().getProperty("apiDefaultPrefix");
		apiEnvName = this.context.getJavaClientGeneratorConfiguration().getProperty("apiEnvName");
	}

	private String getProperty(String key, Properties first, Properties second) {
		if (first != null && StringUtility.stringHasValue(first.getProperty(key))) {
			return first.getProperty(key);
		}
		if (second != null && StringUtility.stringHasValue(second.getProperty(key))) {
			return second.getProperty(key);
		}
		return "";
	}

	protected Interface createBasicInterface() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		String camelCaseName = JavaBeansUtil.convertCamelCase(table.getDomainObjectName(), "-");
		String typescriptModelPackage = this.context.getJavaClientGeneratorConfiguration().getTargetPackage();
		FullyQualifiedTypescriptType type = new FullyQualifiedTypescriptType("",
				typescriptModelPackage + "." + camelCaseName + "." + table.getDomainObjectName(), true);
		Interface interfaze = new Interface(type);
		interfaze.setVisibility(JavaVisibility.PUBLIC);
		context.getCommentGenerator().addJavaFileComment(interfaze);
		return interfaze;
	}

	private TypescriptTopLevelClass generateRestApiClass() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		String modelObjectName = table.getDomainObjectName();
		String camelCaseName = JavaBeansUtil.convertCamelCase(modelObjectName, "-");
		FullyQualifiedTypescriptType tsApiModelJavaType = new FullyQualifiedTypescriptType(this.projectRootAlias,
				this.context.getJavaClientGeneratorConfiguration().getTargetPackage() + "." + camelCaseName + "."
						+ modelObjectName,
				true);
		TypescriptTopLevelClass typescriptTopLevelClass = new TypescriptTopLevelClass(tsApiModelJavaType);

		typescriptTopLevelClass.setWriteMode(WriteMode.OVER_WRITE);

		typescriptTopLevelClass.setVisibility(JavaVisibility.PUBLIC);

		// GeneratorUtils.addComment(typescriptTopLevelClass,
		// topLevelClass.getDescription() + "
		// 服务请求类");

		String typescriptModelPackage = this.context.getJavaModelGeneratorConfiguration().getTargetPackage();

		FullyQualifiedJavaType recordType = new FullyQualifiedJavaType(typescriptModelPackage + "." + modelObjectName);

		FullyQualifiedJavaType requestJavaType = new FullyQualifiedTypescriptType(this.projectRootAlias,
				typescriptModelPackage + "." + WebUtils.getFileName(modelObjectName) + "." + modelObjectName, true);

		IntrospectedColumn pkColumn = GeneratorUtils.getPrimaryKey(introspectedTable);
		WebRestMethodsGenerator restMethodsGenerator = new WebRestMethodsGenerator(recordType, requestJavaType, "",
				pkColumn, false);
		restMethodsGenerator.generate();
		RestMethodAndImports methodAndImports = restMethodsGenerator.build();

		String apiDefaultPrefix = "";
		if (StringUtility.stringHasValue(this.apiDefaultPrefix) && StringUtility.stringHasValue(this.apiEnvName)) {
			Variable constApiPrefix = new Variable("constApiPrefix");
			constApiPrefix.getInitialization()
				.addBodyLine(String.format("`${import.meta.env.%s || '%s'}`", this.apiEnvName, this.apiDefaultPrefix));
			typescriptTopLevelClass.addVariable(constApiPrefix);
			apiDefaultPrefix = "constApiPrefix";
		}

		for (RestMethod method : methodAndImports.getMethods()) {
			String methodName = method.getName();
			String returnTypeName = "void";
			if (method.getReturnType().isPresent()) {
				FullyQualifiedJavaType returnType = method.getReturnType().get();
				if ("net.entframework.kernel.db.api.pojo.page.PageResult"
					.equals(returnType.getFullyQualifiedNameWithoutTypeParameters())) {
					FullyQualifiedJavaType arg = returnType.getTypeArguments().get(0);
					String newTypeName = arg.getShortName() + "PageModel";
					FullyQualifiedJavaType newType = new FullyQualifiedTypescriptType(
							this.projectRootAlias, typescriptModelPackage + "."
									+ JavaBeansUtil.convertCamelCase(arg.getShortName(), "-") + "." + newTypeName,
							true);
					returnTypeName = newTypeName;
					method.setReturnType(newType);
					methodAndImports.getImports().removeIf(javaType -> javaType.equals(returnType));
					methodAndImports.getImports().add(newType);
				}
				else {
					returnTypeName = RenderingUtilities.calculateTypescriptTypeName(null, returnType);
				}
			}
			Parameter parameter = method.getParameters().get(0);
			if (StringUtils.equals("POST", method.getHttpMethod())) {
				if (StringUtility.stringHasValue(apiDefaultPrefix)) {
					method.addBodyLine(String.format("defHttp.post<%s>({ url: `${%s}%s`, data: %s });", returnTypeName,
							apiDefaultPrefix, method.getRestPath(), parameter.getName()));
				}
				else {
					method.addBodyLine(String.format("defHttp.post<%s>({ url: '%s', data: %s });", returnTypeName,
							method.getRestPath(), parameter.getName()));
				}

			}
			if (StringUtils.equals("GET", method.getHttpMethod())) {
				if (StringUtility.stringHasValue(apiDefaultPrefix)) {
					method.addBodyLine(String.format("defHttp.get<%s>({ url: `${%s}%s`, params: %s });", returnTypeName,
							apiDefaultPrefix, method.getRestPath(), parameter.getName()));
				}
				else {
					method.addBodyLine(String.format("defHttp.get<%s>({ url: '%s', params: %s });", returnTypeName,
							method.getRestPath(), parameter.getName()));
				}

			}
			method.setName(modelObjectName + StringUtils.capitalize(methodName));
		}

		methodAndImports.getMethods().forEach(typescriptTopLevelClass::addMethod);
		typescriptTopLevelClass.addImportedTypes(methodAndImports.getImports());
		typescriptTopLevelClass.setAttribute(Constants.WEB_PROJECT_ROOT_ALIAS, this.projectRootAlias);
		typescriptTopLevelClass
			.addImportedType(new FullyQualifiedTypescriptType("", "fe-ent-core.es.utils.defHttp", false));

		return typescriptTopLevelClass;
	}

	@Override
	public AbstractXmlGenerator getMatchedXMLGenerator() {
		return null;
	}

}
