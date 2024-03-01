package net.entframework.kernel.db.generator.plugin.web;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.config.Relation;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import net.entframework.kernel.db.generator.utils.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JoinEntry;
import org.mybatis.generator.config.JoinTable;
import org.mybatis.generator.config.JoinTarget;

import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansFieldWithGeneratedAnnotation;

/**
 * 生成Typescript的Relation关系
 */
public class TypescriptModelPlugin extends AbstractWebPlugin {

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public boolean modelBaseRecordClassGenerated(IntrospectedTable introspectedTable) {
		TopLevelClass topLevelClass = introspectedTable.getBaseModelClass();
		String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
		JoinEntry joinEntry = context.getJoinConfig().getJoinEntry(tableName);
		if (joinEntry != null) {
			joinEntry.validate();
			addJoinField(topLevelClass, joinEntry);
		}
		return true;
	}

	private void addJoinField(TopLevelClass topLevelClass, JoinEntry joinEntry) {
		// Many-to-one / one-to-many 关联
		for (Pair<String, JoinTarget> detail : joinEntry.getDetails()) {
			JoinTarget target = detail.getRight();
			IntrospectedTable rightTable = GeneratorUtils.getIntrospectedTable(context, target.getRightTable());
			IntrospectedTable leftTable = GeneratorUtils.getIntrospectedTable(context, joinEntry.getLeftTable());
			IntrospectedColumn leftTableColumn = GeneratorUtils.getIntrospectedColumnByColumn(leftTable,
					detail.getLeft());
			IntrospectedColumn rightTableColumn = GeneratorUtils.getIntrospectedColumnByColumn(rightTable,
					target.getJoinColumn());
			Field leftField = GeneratorUtils.getFieldByName(topLevelClass, leftTableColumn.getJavaProperty());

			FullyQualifiedJavaType recordType = new FullyQualifiedJavaType(rightTable.getBaseRecordType());
			recordType = WebUtils.convertToTypescriptType(this.context, recordType);

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

			Field field = getJavaBeansFieldWithGeneratedAnnotation(introspectedColumn, context, rightTable,
					topLevelClass);
			field.setAttribute(Constants.FIELD_RELATION_COLUMN, rightTableColumn);

			if (context.getPlugins()
				.modelFieldGenerated(field, topLevelClass, introspectedColumn, rightTable,
						Plugin.ModelClassType.BASE_RECORD)) {
				Relation.Builder builder = Relation.builder();
				topLevelClass.addImportedType(recordType);
				if (target.getType() == JoinTarget.JoinType.ONE_TO_MANY) {
					topLevelClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
					field.setDescription(GeneratorUtils.getFileDescription(rightTable));
					builder.joinType(JoinTarget.JoinType.ONE_TO_MANY)
						.bindField(field)
						.sourceField(leftField)
						.targetTable(rightTable)
						.targetColumn(rightTableColumn);

				}

				if (target.getType() == JoinTarget.JoinType.MANY_TO_ONE) {
					field.setDescription(GeneratorUtils.getFileDescription(rightTable));
					builder.sourceField(leftField)
						.joinType(JoinTarget.JoinType.MANY_TO_ONE)
						.bindField(field)
						.targetTable(rightTable)
						.displayField(GeneratorUtils.getDisplayField(rightTable).getName())
						.targetColumn(GeneratorUtils.getIntrospectedColumnByColumn(rightTable, target.getJoinColumn()));

				}
				field.setAttribute(Constants.FIELD_RELATION, builder.build());
				// 重置Field的注释行
				field.getJavaDocLines().clear();
				GeneratorUtils.addComment(field, field.getDescription());
				// 关联关系的Field 添加到TopLevelClass中, 但是对应的Column并不添加到IntrospectedTable中
				topLevelClass.addField(field);
			}
		}
		// many-to-many
		List<JoinTable> joinTables = joinEntry.getJoinTables();
		for (JoinTable joinTable : joinTables) {
			IntrospectedTable rightTable = GeneratorUtils.getIntrospectedTable(context, joinTable.getRightTable());

			FullyQualifiedJavaType targetBindType = new FullyQualifiedJavaType(rightTable.getBaseRecordType());

			targetBindType = WebUtils.convertToTypescriptType(this.context, targetBindType);

			IntrospectedColumn introspectedColumn = new IntrospectedColumn();
			introspectedColumn.setJavaProperty(joinTable.getProperty());
			introspectedColumn.setContext(context);
			introspectedColumn.setIntrospectedTable(rightTable);
			introspectedColumn.setFullyQualifiedJavaType(new FullyQualifiedJavaType(rightTable.getBaseRecordType()));
			introspectedColumn.setActualColumnName("");

			FullyQualifiedJavaType listJavaType = FullyQualifiedJavaType.getNewListInstance();
			listJavaType.addTypeArgument(targetBindType);
			topLevelClass.addImportedType(targetBindType);

			Field field = new Field(joinTable.getProperty(), listJavaType);
			field.setDescription(GeneratorUtils.getFileDescription(rightTable));
			GeneratorUtils.addComment(field, field.getDescription());
			field.setAttribute(Constants.FIELD_RELATION_COLUMN, introspectedColumn);

			if (context.getPlugins()
				.modelFieldGenerated(field, topLevelClass, introspectedColumn, rightTable,
						Plugin.ModelClassType.BASE_RECORD)) {
				Relation.Builder builder = Relation.builder();
				builder.joinType(JoinTarget.JoinType.MANY_TO_MANY);
				field.setVisibility(JavaVisibility.PRIVATE);
				field.setAttribute(Constants.FIELD_RELATION, builder.build());

				topLevelClass.addField(field);
			}

		}
	}

}
