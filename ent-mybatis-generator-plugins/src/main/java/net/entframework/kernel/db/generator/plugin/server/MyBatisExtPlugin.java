/*
 * ******************************************************************************
 *  * Copyright (c) 2022-2023. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */

package net.entframework.kernel.db.generator.plugin.server;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.config.Relation;
import net.entframework.kernel.db.generator.plugin.AbstractDynamicSQLPlugin;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JoinEntry;
import org.mybatis.generator.config.JoinTable;
import org.mybatis.generator.config.JoinTarget;

import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.*;

public class MyBatisExtPlugin extends AbstractDynamicSQLPlugin {

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
			IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		GeneratorUtils.addFieldComment(field, introspectedColumn);
		String fieldDescription = GeneratorUtils.getFieldDescription(introspectedColumn);
		field.setDescription(fieldDescription);

		if (GeneratorUtils.isPrimaryKey(introspectedTable, introspectedColumn)) {
			field.addAnnotation("@Id");
			topLevelClass.addImportedType("jakarta.persistence.Id");
		}

		introspectedTable.getGeneratedKey().ifPresent(generatedKey -> {
			if (generatedKey.getColumn().equals(introspectedColumn.getActualColumnName())) {
				field.addAnnotation("@GeneratedValue");
				topLevelClass.addImportedType("jakarta.persistence.GeneratedValue");
			}
		});

		if (!GeneratorUtils.isRelationField(field)) {
			StringBuilder columnSB = new StringBuilder(
					String.format("@Column(name = \"%s\"", introspectedColumn.getActualColumnName()));
			if (introspectedColumn.isStringColumn() && !introspectedColumn.isBLOBColumn()) {
				if (introspectedColumn.getLength() > 0 && introspectedColumn.getLength() != 255) {
					columnSB.append(", length=").append(introspectedColumn.getLength());
				}
			}
			if (!introspectedColumn.isNullable()) {
				columnSB.append(", nullable=").append("false");
			}
			if (introspectedColumn.isNumberColumn() && introspectedColumn.getScale() > 0) {
				columnSB.append(", scale=").append(introspectedColumn.getScale());
			}
			columnSB.append(")");
			field.addAnnotation(columnSB.toString());
			topLevelClass.addImportedType("jakarta.persistence.Column");

			StringBuilder columnSql = new StringBuilder("@SqlColumn(");

			if (!StringUtils.equals("OTHER", introspectedColumn.getJdbcTypeName())) {
				columnSql.append("jdbcType = JDBCType.").append(introspectedColumn.getJdbcTypeName());
				topLevelClass.addImportedType(new FullyQualifiedJavaType("java.sql.JDBCType"));
			}
			else {
				throw new RuntimeException(
						"Can't determine jdbc type for column: " + introspectedColumn.getActualColumnName()
								+ " in table :" + introspectedTable.getFullyQualifiedTableNameAtRuntime());
			}
			if (StringUtils.isNotEmpty(introspectedColumn.getTypeHandler())) {
				FullyQualifiedJavaType typeHandler = new FullyQualifiedJavaType(introspectedColumn.getTypeHandler());
				topLevelClass.addImportedType(typeHandler);
				columnSql.append(", typeHandler = ").append(typeHandler.getShortName()).append(".class");
			}
			columnSql.append(")");
			field.addAnnotation(columnSql.toString());
			topLevelClass.addImportedType("org.mybatis.dynamic.sql.annotation.SqlColumn");

		}

		if (StringUtils.equalsIgnoreCase(introspectedColumn.getActualColumnName(),
				introspectedTable.getTableConfiguration().getLogicDeleteColumn())) {
			field.addAnnotation("@LogicDelete");
			topLevelClass.addImportedType("org.mybatis.dynamic.sql.annotation.LogicDelete");
			field.setAttribute(Constants.FIELD_LOGIC_DELETE_ATTR, true);
		}

		if (StringUtils.equalsIgnoreCase(introspectedColumn.getActualColumnName(),
				introspectedTable.getTableConfiguration().getTenantColumn())) {
			field.addAnnotation("@Tenant");
			topLevelClass.addImportedType("org.mybatis.dynamic.sql.annotation.Tenant");
			field.setAttribute(Constants.FIELD_TENANT_ATTR, true);
		}

		if (StringUtils.equalsIgnoreCase(introspectedColumn.getActualColumnName(),
				introspectedTable.getTableConfiguration().getVersionColumn())) {
			field.addAnnotation("@Version");
			topLevelClass.addImportedType("jakarta.persistence.Version");
			field.setAttribute(Constants.FIELD_VERSION_ATTR, true);
		}

		introspectedColumn.getProperties().put(Constants.INTROSPECTED_COLUMN_FIELD_BINDING, field);

		// 补充验证信息
		if (!GeneratorUtils.isPrimaryKey(introspectedTable, introspectedColumn)
				&& !GeneratorUtils.isRelationField(field)) {

			if (!introspectedColumn.isNullable()) {
				if (GeneratorUtils.isStringField(field)) {
					field.addAnnotation(String.format("@NotBlank(message = \"%s不能为空\")", fieldDescription));
					topLevelClass.addImportedType("jakarta.validation.constraints.NotBlank");
				}
				else {
					field.addAnnotation(String.format("@NotNull(message = \"%s不能为空\")", fieldDescription));
					topLevelClass.addImportedType("jakarta.validation.constraints.NotNull");
				}
			}

			if (GeneratorUtils.isStringField(field) && !introspectedColumn.isBLOBColumn()) {
				field.addAnnotation(String.format("@Size(max = %s, message = \"%s长度超出\")",
						introspectedColumn.getLength(), fieldDescription));
				topLevelClass.addImportedType("jakarta.validation.constraints.Size");
			}
		}

		if (introspectedColumn.isBlobColumn() || introspectedColumn.isClobColumn()) {
			field.addAnnotation("@Lob");
			topLevelClass.addImportedType("jakarta.persistence.Lob");
		}

		return true;
	}

	/**
	 * Intercepts base record class generation
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		String fileDescription = GeneratorUtils.getFileDescription(introspectedTable);
		topLevelClass.setDescription(fileDescription);

		if (introspectedTable.isMappedSuperclass()) {
			topLevelClass.addAnnotation("@MappedSuperclass");
			topLevelClass.addImportedType("jakarta.persistence.MappedSuperclass");
		}
		topLevelClass.addAnnotation("@Entity");
		topLevelClass.addImportedType("jakarta.persistence.Entity");
		topLevelClass.addAnnotation(
				String.format("@Table(name = \"%s\")", introspectedTable.getFullyQualifiedTableNameAtRuntime()));

		topLevelClass.addImportedType("jakarta.persistence.Table");

		return true;
	}

	@Override
	public boolean modelBaseRecordClassGenerated(IntrospectedTable introspectedTable) {
		TopLevelClass topLevelClass = introspectedTable.getBaseModelClass();
		String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
		JoinEntry joinEntry = context.getJoinConfig().getJoinEntry(tableName);
		if (joinEntry != null) {
			addJoinField(topLevelClass, joinEntry);
		}

		return true;
	}

	@Override
	public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
		String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
		JoinEntry joinEntry = context.getJoinConfig().getJoinEntry(tableName);
		if (joinEntry != null) {
			// 验证join配置
			joinEntry.validate();
		}

		String fileDescription = GeneratorUtils.getFileDescription(introspectedTable);
		interfaze.setDescription(fileDescription);

		FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(
				"net.entframework.kernel.db.mybatis.mapper.BaseMapper");
		fqjt.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
		interfaze.addSuperInterface(fqjt);
		interfaze.addImportedType(fqjt);
		return true;
	}

	private void addJoinField(TopLevelClass topLevelClass, JoinEntry joinEntry) {
		// Many-to-one / one-to-many 关联
		for (Pair<String, JoinTarget> detail : joinEntry.getDetails()) {
			JoinTarget target = detail.getRight();
			IntrospectedTable rightTable = GeneratorUtils.getIntrospectedTable(context, target.getRightTable());
			IntrospectedTable leftTable = GeneratorUtils.getIntrospectedTable(context, joinEntry.getLeftTable());

			FullyQualifiedJavaType recordType = new FullyQualifiedJavaType(rightTable.getBaseRecordType());
			FullyQualifiedJavaType listReturnType = FullyQualifiedJavaType.getNewListInstance();
			listReturnType.addTypeArgument(recordType);
			FullyQualifiedJavaType filedType = target.getType() == JoinTarget.JoinType.ONE_TO_MANY ? listReturnType
					: recordType;
			IntrospectedColumn introspectedColumn = new IntrospectedColumn();
			introspectedColumn.setJavaProperty(target.getFieldName());
			introspectedColumn.setContext(context);
			introspectedColumn.setIntrospectedTable(rightTable);
			introspectedColumn.setFullyQualifiedJavaType(filedType);
			introspectedColumn.setActualColumnName(target.getJoinColumn());
			IntrospectedColumn rightTableColumn = GeneratorUtils.getIntrospectedColumnByColumn(rightTable,
					target.getJoinColumn());

			Field field = getJavaBeansFieldWithGeneratedAnnotation(introspectedColumn, context, rightTable,
					topLevelClass);
			field.setAttribute(Constants.FIELD_RELATION, true);
			if (context.getPlugins()
				.modelFieldGenerated(field, topLevelClass, introspectedColumn, rightTable,
						Plugin.ModelClassType.BASE_RECORD)) {
				Relation.Builder builder = Relation.builder();
				topLevelClass.addImportedType(recordType);
				if (target.getType() == JoinTarget.JoinType.ONE_TO_MANY) {
					topLevelClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
					IntrospectedColumn leftTableColumn = GeneratorUtils.getIntrospectedColumnByColumn(leftTable,
							detail.getLeft());
					IntrospectedColumn leftKeyColumn = GeneratorUtils.getIntrospectedColumnByColumn(leftTable,
							detail.getKey());
					field.setDescription(GeneratorUtils.getFileDescription(rightTable));
					builder.joinType(JoinTarget.JoinType.ONE_TO_MANY)
						.bindField(field)
						.sourceField(GeneratorUtils.getFieldByName(topLevelClass, leftKeyColumn.getJavaProperty()))
						.targetTable(rightTable)
						.targetColumn(rightTableColumn);

					field.addAnnotation("@OneToMany");
					field.addAnnotation(String.format("@JoinColumn(name = \"%s\", referencedColumnName = \"%s\")",
							leftTableColumn.getActualColumnName(), rightTableColumn.getActualColumnName()));
					topLevelClass.addImportedType("jakarta.persistence.OneToMany");
					topLevelClass.addImportedType("jakarta.persistence.JoinColumn");
				}

				if (target.getType() == JoinTarget.JoinType.MANY_TO_ONE) {
					String columnName = detail.getKey();
					IntrospectedColumn leftColumn = GeneratorUtils.getIntrospectedColumnByColumn(leftTable, columnName);
					Field relatedField = GeneratorUtils.getFieldByName(topLevelClass, leftColumn.getJavaProperty());
					field.setDescription(relatedField.getDescription());
					builder.sourceField(relatedField)
						.joinType(JoinTarget.JoinType.MANY_TO_ONE)
						.bindField(field)
						.targetTable(rightTable)
						.displayField(GeneratorUtils.getDisplayField(rightTable).getName())
						.targetColumn(GeneratorUtils.getIntrospectedColumnByColumn(rightTable, target.getJoinColumn()));
					field.addAnnotation("@ManyToOne");
					field.addAnnotation(String.format("@JoinColumn(name = \"%s\", referencedColumnName = \"%s\")",
							leftColumn.getActualColumnName(), rightTableColumn.getActualColumnName()));
					topLevelClass.addImportedType("jakarta.persistence.ManyToOne");
					topLevelClass.addImportedType("jakarta.persistence.JoinColumn");
				}
				field.setAttribute(Constants.FIELD_RELATION, builder.build());
				// 重置Field的注释行
				field.getJavaDocLines().clear();
				GeneratorUtils.addComment(field, field.getDescription());
				// 关联关系的Field 添加到TopLevelClass中, 但是对应的Column并不添加到IntrospectedTable中
				topLevelClass.addField(field);
			}

			Method method = getJavaBeansGetterWithGeneratedAnnotation(introspectedColumn, context, rightTable,
					topLevelClass);
			if (context.getPlugins()
				.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, rightTable,
						Plugin.ModelClassType.BASE_RECORD)) {
				topLevelClass.addMethod(method);
			}

			if (!rightTable.isImmutable()) {
				method = getJavaBeansSetterWithGeneratedAnnotation(introspectedColumn, context, rightTable,
						topLevelClass);
				if (context.getPlugins()
					.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, rightTable,
							Plugin.ModelClassType.BASE_RECORD)) {
					topLevelClass.addMethod(method);
				}
			}

		}
		// many-to-many
		List<JoinTable> joinTables = joinEntry.getJoinTables();
		for (JoinTable joinTable : joinTables) {
			IntrospectedTable rightTable = GeneratorUtils.getIntrospectedTable(context, joinTable.getRightTable());
			IntrospectedTable middleTable = GeneratorUtils.getIntrospectedTable(context, joinTable.getMiddleTable());

			FullyQualifiedJavaType targetBindType = new FullyQualifiedJavaType(rightTable.getBaseRecordType());

			IntrospectedColumn introspectedColumn = new IntrospectedColumn();
			introspectedColumn.setJavaProperty(joinTable.getProperty());
			introspectedColumn.setContext(context);
			introspectedColumn.setIntrospectedTable(rightTable);
			introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType(rightTable.getBaseRecordType()));
			introspectedColumn.setActualColumnName("");
			introspectedColumn.setRemarks(StringUtils.isNotEmpty(rightTable.getRemarks()) ? rightTable.getRemarks()
					: rightTable.getTableConfiguration().getDomainObjectName());

			FullyQualifiedJavaType listJavaType = FullyQualifiedJavaType.getNewListInstance();
			listJavaType.addTypeArgument(targetBindType);
			topLevelClass.addImportedType(listJavaType);

			Field field = new Field(joinTable.getProperty(), listJavaType);
			field.setAttribute(Constants.FIELD_RELATION, true);
			if (context.getPlugins()
				.modelFieldGenerated(field, topLevelClass, introspectedColumn, rightTable,
						Plugin.ModelClassType.BASE_RECORD)) {
				Relation.Builder builder = Relation.builder();
				builder.joinType(JoinTarget.JoinType.MANY_TO_MANY);
				field.setVisibility(JavaVisibility.PRIVATE);
				field.setAttribute(Constants.FIELD_RELATION, builder.build());

				field.addAnnotation("@ManyToMany");
				field.addAnnotation(String.format(
						"@JoinTable(name = \"%s\", joinColumns = @JoinColumn(name = \"%s\", referencedColumnName = \"%s\"), inverseJoinColumns = @JoinColumn(name = \"%s\", referencedColumnName = \"%s\"))",
						middleTable.getFullyQualifiedTableNameAtRuntime(), joinTable.getJoinColumn().getColumnName(),
						joinTable.getJoinColumn().getReferencedColumnName(),
						joinTable.getInverseJoinColumn().getColumnName(),
						joinTable.getInverseJoinColumn().getReferencedColumnName()));
				topLevelClass.addImportedType("jakarta.persistence.ManyToMany");
				topLevelClass.addImportedType("jakarta.persistence.JoinTable");
				topLevelClass.addImportedType("jakarta.persistence.JoinColumn");

				topLevelClass.addField(field);
			}

		}
	}

}
