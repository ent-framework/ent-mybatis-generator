package net.entframework.kernel.db.generator.utils;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.config.Relation;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import net.entframework.kernel.db.generator.typescript.runtime.FullyQualifiedTypescriptType;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JoinTarget;
import org.mybatis.generator.config.UIConfig;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.*;
import java.util.stream.Collectors;

public class WebUtils {

	public static String getFileName(String shortName) {
		return JavaBeansUtil.convertCamelCase(shortName, "-");
		// return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, shortName);
		// //$NON-NLS-1$
	}

	public static Field getTypescriptField(IntrospectedColumn introspectedColumn, Context context,
			IntrospectedTable introspectedTable, CompilationUnit compilationUnit) {
		FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
		String property = introspectedColumn.getJavaProperty();

		Field field = new Field(property, fqjt);
		field.setVisibility(JavaVisibility.PRIVATE);

		return field;
	}

	public static FullyQualifiedTypescriptType convertToTypescriptType(Context context, FullyQualifiedJavaType type) {

		String projectRootAlias = getProjectAlias(context);
		String shortName = type.getShortName();
		String packageName = type.getPackageName();
		String camelCaseName = JavaBeansUtil.convertCamelCase(shortName, "-");
		return new FullyQualifiedTypescriptType(projectRootAlias, packageName + "." + camelCaseName + "." + shortName,
				true);
	}

	public static String getProjectAlias(Context context) {
		String projectAlias = context.getProperty("projectRootAlias");
		if (StringUtils.isNotBlank(projectAlias)) {
			return projectAlias;
		}
		return "";
	}

	public static FullyQualifiedTypescriptType convertToTypescriptImportType(String projectRootAlias,
			FullyQualifiedJavaType type) {
		String shortName = type.getShortName();
		String packageName = type.getPackageName();
		return new FullyQualifiedTypescriptType(projectRootAlias,
				packageName + "." + getFileName(shortName) + "." + shortName, true);
	}

	public static List<Field> getFieldsWithoutPrimaryKey(List<Field> fields, String pkField) {
		return fields.stream()
			.filter(field -> !StringUtils.equals(field.getName(), pkField))
			.collect(Collectors.toList());
	}

	/**
	 * 筛选列表展示字段
	 * @param fields
	 * @return
	 */
	public static List<Field> getListFields(List<Field> fields, Set<String> ignoreFields,
			IntrospectedTable introspectedTable) {
		UIConfig uiConfig = introspectedTable.getTableConfiguration().getUiConfig();
		Set<String> definedFields = new HashSet<>();
		Set<String> ignoredFields = new HashSet<>(ignoreFields);
		if (uiConfig != null && uiConfig.getListFields() != null) {
			definedFields.addAll(uiConfig.getListFields().getFields());
			ignoredFields.addAll(uiConfig.getListFields().getIgnored());
		}
		return filterFields(fields, definedFields, ignoredFields);
	}

	public static List<Field> getSearchFields(List<Field> fields, IntrospectedTable introspectedTable) {
		UIConfig uiConfig = introspectedTable.getTableConfiguration().getUiConfig();
		Set<String> definedFields = new HashSet<>();
		Set<String> ignoredFields = new HashSet<>();
		if (uiConfig != null && uiConfig.getSearchable() != null) {
			definedFields.addAll(uiConfig.getSearchable().getFields());
			ignoredFields.addAll(uiConfig.getSearchable().getIgnored());
		}
		if (definedFields.isEmpty() && ignoredFields.isEmpty()) {
			return Collections.emptyList();
		}
		return filterFields(fields, definedFields, ignoredFields);
	}

	private static List<Field> filterFields(List<Field> fields, Set<String> definedFields, Set<String> ignoredFields) {
		return fields.stream().filter(field -> {

			if (GeneratorUtils.isLogicDeleteField(field) || GeneratorUtils.isVersionField(field)) {
				return false;
			}

			if (!definedFields.isEmpty()) {
				return definedFields.contains(field.getName());
			}

			if (ignoredFields.contains(field.getName())) {
				return false;
			}

			if (GeneratorUtils.isRelationField(field)) {
				Relation relation = (Relation) field.getAttribute(Constants.FIELD_RELATION);
				if (relation.getJoinType() != null && relation.getJoinType() == JoinTarget.JoinType.MANY_TO_ONE) {
					return true;
				}
				else {
					return false;
				}
			}

			return true;
		}).collect(Collectors.toList());
	}

	private static Optional<Field> getBeRelatedCommonField(Field field, List<Field> manyToOneFields) {
		return manyToOneFields.stream().filter(field1 -> {
			Relation relation = (Relation) field1.getAttribute(Constants.FIELD_RELATION);
			return StringUtils.equals(field.getName(), relation.getSourceField().getName());
		}).findFirst();
	}

	/**
	 * 筛选Form输入字段
	 * @param fields fields
	 * @param ignoreFields ignoreFields
	 * @return field list
	 */
	public static List<Field> getInputFields(List<Field> fields, Set<String> ignoreFields,
			IntrospectedTable introspectedTable) {
		List<Field> manyToOneFields = GeneratorUtils.getRelatedFields(fields, JoinTarget.JoinType.MANY_TO_ONE);
		UIConfig uiConfig = introspectedTable.getTableConfiguration().getUiConfig();
		Set<String> definedFields = new HashSet<>();
		Set<String> ignoredFields = new HashSet<>();

		return fields.stream().filter(field -> {
			if (GeneratorUtils.isRelationField(field)) {
				return false;
			}
			if (GeneratorUtils.isLogicDeleteField(field) || GeneratorUtils.isVersionField(field)) {
				return false;
			}

			if (ignoreFields.contains(field.getName())) {
				return false;
			}

			Optional<Field> beRelated = getBeRelatedCommonField(field, manyToOneFields);
			if (beRelated.isPresent()) {
				field.setAttribute(Constants.TARGET_FIELD_RELATION,
						beRelated.get().getAttribute(Constants.FIELD_RELATION));
				return true;
			}
			return field.getAttribute(Constants.FIELD_EXT_ATTR) == null;
		}).collect(Collectors.toList());
	}

	public static List<Field> getRelationFields(List<Field> fields) {
		return GeneratorUtils.getRelatedFields(fields, JoinTarget.JoinType.MANY_TO_ONE);
	}

}
