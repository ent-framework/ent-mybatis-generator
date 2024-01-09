/*
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.runtime.dynamic.sql;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansFieldWithGeneratedAnnotation;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansGetterWithGeneratedAnnotation;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansSetterWithGeneratedAnnotation;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.internal.util.StringUtility;

/**
 * This model generator builds a flat model with default constructor and getters/setters.
 * It does not support the immutable model, or constructor based attributes.
 *
 * @author Jeff Butler
 *
 */
public class DynamicSqlModelGenerator extends AbstractJavaGenerator {

	public DynamicSqlModelGenerator(String project) {
		super(project);
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.8", table.toString())); //$NON-NLS-1$
		Plugin plugins = context.getPlugins();
		CommentGenerator commentGenerator = context.getCommentGenerator();

		List<IntrospectedTable> tables = context.getIntrospectedTables();
		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(topLevelClass);
		FullyQualifiedJavaType superClass;
		boolean parentTableFound = false;
		if (StringUtility.stringHasValue(introspectedTable.getTableConfiguration().getParentTable())) {
			// Has parent table defined
			IntrospectedTable parentTable = findParentTable(tables,
					introspectedTable.getTableConfiguration().getParentTable());
			if (parentTable != null) {
				parentTableFound = true;
				superClass = new FullyQualifiedJavaType(parentTable.getBaseRecordType());
				topLevelClass.setSuperClass(superClass);
				topLevelClass.addImportedType(superClass);
			}
		}
		if (!parentTableFound) {
			superClass = getSuperClass();
			if (superClass != null) {
				topLevelClass.setSuperClass(superClass);
				topLevelClass.addImportedType(superClass);
			}
		}

		commentGenerator.addModelClassComment(topLevelClass, introspectedTable);

		List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();

		if (introspectedTable.isConstructorBased()) {
			addParameterizedConstructor(topLevelClass);

			if (!introspectedTable.isImmutable()) {
				addDefaultConstructorWithGeneratedAnnotatoin(topLevelClass);
			}
		}

		String rootClass = getRootClass();
		RootClassInfo rootClassInfo = RootClassInfo.getInstance(rootClass, warnings);
		if (rootClassInfo == null) {
			throw new RuntimeException("Can't get root class");
		}
		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			if (rootClassInfo.containsProperty(introspectedColumn)) {
				continue;
			}

			Field field = getJavaBeansFieldWithGeneratedAnnotation(introspectedColumn, context, introspectedTable,
					topLevelClass);

			if (plugins.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable,
					Plugin.ModelClassType.BASE_RECORD)) {
				topLevelClass.addField(field);
				topLevelClass.addImportedType(field.getType());
			}

			Method method = getJavaBeansGetterWithGeneratedAnnotation(introspectedColumn, context, introspectedTable,
					topLevelClass);
			if (plugins.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable,
					Plugin.ModelClassType.BASE_RECORD)) {
				topLevelClass.addMethod(method);
			}

			if (!introspectedTable.isImmutable()) {
				method = getJavaBeansSetterWithGeneratedAnnotation(introspectedColumn, context, introspectedTable,
						topLevelClass);
				if (plugins.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable,
						Plugin.ModelClassType.BASE_RECORD)) {
					topLevelClass.addMethod(method);
				}
			}
		}

		List<CompilationUnit> answer = new ArrayList<>();
		if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
			answer.add(topLevelClass);
		}
		return answer;
	}

	private IntrospectedTable findParentTable(List<IntrospectedTable> tables, String parentTable) {
		return tables.stream()
			.filter(introspectedTable1 -> introspectedTable1.getFullyQualifiedTable()
				.getIntrospectedTableName()
				.equals(parentTable))
			.findFirst()
			.orElse(null);
	}

	private FullyQualifiedJavaType getSuperClass() {
		FullyQualifiedJavaType superClass;
		String rootClass = getRootClass();
		if (rootClass != null) {
			superClass = new FullyQualifiedJavaType(rootClass);
		}
		else {
			superClass = null;
		}

		return superClass;
	}

	private void addParameterizedConstructor(TopLevelClass topLevelClass) {
		Method method = new Method(topLevelClass.getType().getShortName());
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setConstructor(true);
		context.getCommentGenerator()
			.addGeneralMethodAnnotation(method, introspectedTable, topLevelClass.getImportedTypes());

		List<IntrospectedColumn> constructorColumns = introspectedTable.getAllColumns();

		for (IntrospectedColumn introspectedColumn : constructorColumns) {
			method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(),
					introspectedColumn.getJavaProperty()));
		}

		StringBuilder sb = new StringBuilder();
		List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();
		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			sb.setLength(0);
			sb.append("this."); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.append(" = "); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.append(';');
			method.addBodyLine(sb.toString());
		}

		topLevelClass.addMethod(method);
	}

}
