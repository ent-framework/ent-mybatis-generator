/*
 * ******************************************************************************
 *  * Copyright (c) 2022. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */
package net.entframework.kernel.db.generator.plugin.server.methods.repository;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import net.entframework.kernel.db.generator.plugin.server.methods.AbstractMethodGenerator;
import net.entframework.kernel.db.generator.plugin.server.methods.MethodAndImports;
import net.entframework.kernel.db.generator.utils.CommentHelper;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;

import java.util.*;

public class RepEnhancedCreateAndUpdateMethodGenerator extends AbstractMethodGenerator {

	public RepEnhancedCreateAndUpdateMethodGenerator(BuildConfig builder) {
		super(builder);
	}

	@Override
	public MethodAndImports generateMethodAndImports() {
		Set<FullyQualifiedJavaType> imports = new HashSet<>();
		Set<String> staticImports = new HashSet<>();

		imports.add(recordType);

		Method createMethod = new Method("insert"); //$NON-NLS-1$
		createMethod.setAbstract(isAbstract);
		createMethod.setReturnType(recordType);
		createMethod.addParameter(new Parameter(recordType, "row")); //$NON-NLS-1$

		Method insertSelective = new Method("insertSelective"); //$NON-NLS-1$
		insertSelective.setAbstract(isAbstract);
		insertSelective.setReturnType(recordType);
		insertSelective.addParameter(new Parameter(recordType, "row")); //$NON-NLS-1$

		Method updateMethod = new Method("updateByPrimaryKey"); //$NON-NLS-1$
		updateMethod.setAbstract(isAbstract);
		updateMethod.setReturnType(recordType);
		updateMethod.addParameter(new Parameter(recordType, "row")); //$NON-NLS-1$

		Method batchCreateMethod = new Method("insertMultiple"); //$NON-NLS-1$
		batchCreateMethod.setAbstract(isAbstract);
		FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
		returnType.addTypeArgument(recordType);
		batchCreateMethod.setReturnType(returnType);

		FullyQualifiedJavaType parameterType = FullyQualifiedJavaType.getNewListInstance(); // $NON-NLS-1$
		parameterType.addTypeArgument(recordType);
		imports.add(parameterType);
		batchCreateMethod.addParameter(new Parameter(parameterType, "records")); //$NON-NLS-1$

		if (this.isAbstract) {
			Map<String, Object> variables = new HashMap<>();
			variables.put("RepositoryName", getRepositoryJavaType().getShortName());
			variables.put("EntityName", recordType.getShortName());
			GeneratorUtils.addComment(createMethod,
					CommentHelper.INSTANCE.getComments("create", "Repository", variables));
			GeneratorUtils.addComment(updateMethod,
					CommentHelper.INSTANCE.getComments("update", "Repository", variables));
			GeneratorUtils.addComment(batchCreateMethod,
					CommentHelper.INSTANCE.getComments("batchCreate", "Repository", variables));
		}
		else {
			GeneratorUtils.addComment(createMethod, "{@inheritDoc}");
			GeneratorUtils.addComment(updateMethod, "{@inheritDoc}");
			GeneratorUtils.addComment(batchCreateMethod, "{@inheritDoc}");
		}
		if (!isAbstract) {
			createMethod.addAnnotation("@Override");
			createMethod.setVisibility(JavaVisibility.PUBLIC);

			insertSelective.addAnnotation("@Override");
			insertSelective.setVisibility(JavaVisibility.PUBLIC);

			updateMethod.addAnnotation("@Override");
			updateMethod.setVisibility(JavaVisibility.PUBLIC);

			batchCreateMethod.addAnnotation("@Override");
			batchCreateMethod.setVisibility(JavaVisibility.PUBLIC);

			Method setDefaultValueMethod = new Method("setDefaultValue"); //$NON-NLS-1$
			setDefaultValueMethod.setVisibility(JavaVisibility.PUBLIC);
			setDefaultValueMethod.setReturnType(recordType);
			setDefaultValueMethod.addParameter(new Parameter(recordType, "row")); //$NON-NLS-1$

			List<IntrospectedColumn> allColumns = introspectedTable.getNonPrimaryKeyColumns();
			for (IntrospectedColumn column : allColumns) {
				Field field = (Field) column.getProperties().get(Constants.INTROSPECTED_COLUMN_FIELD_BINDING);
				if (field != null) {
					if (column.getDefaultValue() != null) {
						String defaultValue = column.getDefaultValue();
						if (column.isNumberColumn()) {
							switch (field.getType().getFullyQualifiedName()) {
								case "java.lang.Long":
									if (!defaultValue.toUpperCase().endsWith("L")) {
										defaultValue += "L";
									}
									break;
								case "java.lang.Double":
									if (!defaultValue.toUpperCase().endsWith("D")) {
										defaultValue += "D";
									}
									break;
							}
						}
						if (defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
							defaultValue = "\"" + defaultValue.substring(1, defaultValue.length() - 1) + "\"";
						}
						if ("CURRENT_TIMESTAMP".equals(defaultValue)) {
							switch (field.getType().getFullyQualifiedName()) {
								case "java.time.LocalDateTime":
									defaultValue = "LocalDateTime.now()";
									imports.add(new FullyQualifiedJavaType("java.time.LocalDateTime"));
									break;
								case "java.util.Date":
									defaultValue = "new Date()";
									imports.add(new FullyQualifiedJavaType("java.util.Date"));
									break;
							}
						}
						setDefaultValueMethod.addBodyLine(String.format("if (Objects.isNull(row.get%s())) {",
								StringUtils.capitalize(field.getName())));
						setDefaultValueMethod.addBodyLine(
								String.format("row.set%s(%s);", StringUtils.capitalize(field.getName()), defaultValue));
						setDefaultValueMethod.addBodyLine("}");
						imports.add(new FullyQualifiedJavaType("java.util.Objects"));
						if (field.getType().isExplicitlyImported()) {
							imports.add(field.getType());
						}
					}
				}
			}
			if (!setDefaultValueMethod.getBodyLines().isEmpty()) {
				setDefaultValueMethod.addBodyLine("return row;");

				createMethod.addBodyLine("return super.insert(setDefaultValue(row));");
				insertSelective.addBodyLine("return super.insertSelective(setDefaultValue(row));");
				updateMethod.addBodyLine("return super.updateByPrimaryKey(setDefaultValue(row));");

				batchCreateMethod.addBodyLine("if (records == null || records.isEmpty()) {");
				batchCreateMethod.addBodyLine("return Collections.emptyList();");
				batchCreateMethod.addBodyLine("}");
				batchCreateMethod.addBodyLine(
						"return super.insertMultiple(records.stream().map(this::setDefaultValue).collect(Collectors.toList()));");

				imports.add(new FullyQualifiedJavaType("java.util.stream.Collectors"));
				imports.add(new FullyQualifiedJavaType("java.util.Collections"));

				return MethodAndImports.withMethod(setDefaultValueMethod)
					.withMethod(createMethod)
					.withMethod(insertSelective)
					.withMethod(batchCreateMethod)
					.withMethod(updateMethod)
					.withImports(imports)
					.withStaticImports(staticImports)
					.build();
			}
		}

		return null;
	}

}
