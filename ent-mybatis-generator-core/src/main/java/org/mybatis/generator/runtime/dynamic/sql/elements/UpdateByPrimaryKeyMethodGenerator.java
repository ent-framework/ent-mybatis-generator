/*
 *    Copyright 2006-2021 the original author or authors.
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
package org.mybatis.generator.runtime.dynamic.sql.elements;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;

public class UpdateByPrimaryKeyMethodGenerator extends AbstractMethodGenerator {

	private final FullyQualifiedJavaType recordType;

	private final FragmentGenerator fragmentGenerator;

	private UpdateByPrimaryKeyMethodGenerator(Builder builder) {
		super(builder);
		recordType = builder.recordType;
		fragmentGenerator = builder.fragmentGenerator;
	}

	@Override
	public MethodAndImports generateMethodAndImports() {
		if (!Utils.generateUpdateByPrimaryKey(introspectedTable)) {
			return null;
		}

		Set<FullyQualifiedJavaType> imports = new HashSet<>();

		imports.add(recordType);

		Method method = new Method("updateByPrimaryKey"); //$NON-NLS-1$
		method.setDefault(true);
		context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, imports);

		method.setReturnType(FullyQualifiedJavaType.getIntInstance());
		method.addParameter(new Parameter(recordType, "row")); //$NON-NLS-1$

		method.addBodyLine("return update(c ->"); //$NON-NLS-1$

		MethodAndImports.Builder builder = MethodAndImports.withMethod(method).withImports(imports);
		Optional<IntrospectedColumn> versionColumnOpt = Utils.getVersionColumn(introspectedTable);
		if (versionColumnOpt.isPresent()) {
			IntrospectedColumn versionColumn = versionColumnOpt.get();
			List<IntrospectedColumn> columns = introspectedTable.getNonPrimaryKeyColumns()
				.stream()
				.filter(column -> !StringUtils.equals(column.getActualColumnName(),
						versionColumn.getActualColumnName()))
				.collect(Collectors.toList());
			method.addBodyLines(fragmentGenerator.getSetEqualLines(columns, "    c", "    ", false)); //$NON-NLS-1$ //$NON-NLS-2$
			String versionSetValue = Utils.getSetVersionValue(versionColumn, tableFieldName, builder);
			if (StringUtils.isNotEmpty(versionSetValue)) {
				method.addBodyLine(versionSetValue);
			}
			method.addBodyLines(fragmentGenerator.getPrimaryKeyWhereClauseForUpdate("    ")); //$NON-NLS-1$
			method.addBodyLine(Utils.setVersionWhereClauseForUpdate(versionColumn, tableFieldName));
		}
		else {
			method.addBodyLines(fragmentGenerator.getSetEqualLines(introspectedTable.getNonPrimaryKeyColumns(), "    c", //$NON-NLS-1$
					"    ", false)); //$NON-NLS-1$
			method.addBodyLines(fragmentGenerator.getPrimaryKeyWhereClauseForUpdate("    ")); //$NON-NLS-1$
		}

		method.addBodyLine(");"); //$NON-NLS-1$
		return builder.build();
	}

	@Override
	public boolean callPlugins(Method method, Interface interfaze) {
		return context.getPlugins()
			.clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
	}

	public static class Builder extends BaseBuilder<Builder> {

		private FullyQualifiedJavaType recordType;

		private FragmentGenerator fragmentGenerator;

		public Builder withRecordType(FullyQualifiedJavaType recordType) {
			this.recordType = recordType;
			return this;
		}

		public Builder withFragmentGenerator(FragmentGenerator fragmentGenerator) {
			this.fragmentGenerator = fragmentGenerator;
			return this;
		}

		@Override
		public Builder getThis() {
			return this;
		}

		public UpdateByPrimaryKeyMethodGenerator build() {
			return new UpdateByPrimaryKeyMethodGenerator(this);
		}

	}

}
