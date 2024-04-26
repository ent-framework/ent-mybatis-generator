package net.entframework.kernel.db.generator.utils;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.config.Relation;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import net.entframework.kernel.db.generator.typescript.runtime.FullyQualifiedTypescriptType;
import net.entframework.kernel.db.generator.typescript.runtime.ModelField;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
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
	 * 明细页展示
	 * @param fields
	 * @return
	 */
	public static List<ModelField> getDetailFields(List<ModelField> fields) {
		List<ModelField> results = new ArrayList<>();

		results.addAll(fields.stream()
			.filter(modelField -> !(modelField.isTenantField() || modelField.isLogicDeleteField()))
			.toList());

		return results;
	}

	/**
	 * 筛选列表展示字段
	 * @param fields
	 * @return
	 */
	public static List<ModelField> getListFields(List<ModelField> fields, Set<String> ignoreFields,
			IntrospectedTable introspectedTable) {

		UIConfig uiConfig = introspectedTable.getTableConfiguration().getUiConfig();
		Set<String> definedFields = new HashSet<>();
		Set<String> ignoredFields = new HashSet<>(ignoreFields);
		if (uiConfig != null && uiConfig.getListFields() != null) {
			definedFields.addAll(uiConfig.getListFields().getFields());
			ignoredFields.addAll(uiConfig.getListFields().getIgnored());
		}

		List<ModelField> results = new ArrayList<>();
		// 先过滤掉逻辑删除字段和Version字段
		for (ModelField field : fields.stream()
			.filter(field -> !(field.isLogicDeleteField() || field.isVersionField() || field.isTenantField())
					|| field.isBlob())
			.toList()) {

			if (field.isOneToMany() || field.isManyToMany()) {
				continue;
			}

			if (field.isBlob()) {
				continue;
			}

			if (ignoredFields.contains(field.getName())) {
				field.setHidden(true);
				results.add(field);
				continue;
			}

			if (!definedFields.isEmpty()) {
				if (!definedFields.contains(field.getName())) {
					field.setHidden(true);
				}
			}
			if (field.isPrimaryKey()) {
				field.setHidden(true);
			}
			results.add(field);

		}
		return results;
	}

	/**
	 * 查询字段配置
	 * @param fields
	 * @param introspectedTable
	 * @return
	 */
	public static List<ModelField> getSearchFields(List<ModelField> fields, IntrospectedTable introspectedTable) {
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
		return filterFields(
				fields.stream().filter(field -> !(field.isLogicDeleteField() || field.isVersionField())).toList(),
				definedFields, ignoredFields);
	}

	private static List<ModelField> filterFields(List<ModelField> fields, Set<String> definedFields,
			Set<String> ignoredFields) {
		return fields.stream().filter(field -> {

			if (!definedFields.isEmpty()) {
				return definedFields.contains(field.getName());
			}

			if (ignoredFields.contains(field.getName())) {
				return false;
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
	public static List<ModelField> getInputFields(List<ModelField> fields, Set<String> ignoreFields,
			IntrospectedTable introspectedTable) {
		UIConfig uiConfig = introspectedTable.getTableConfiguration().getUiConfig();
		Set<String> definedFields = new HashSet<>();
		Set<String> ignoredFields = new HashSet<>(ignoreFields);
		if (uiConfig != null && uiConfig.getInputFields() != null) {
			definedFields.addAll(uiConfig.getInputFields().getFields());
			ignoredFields.addAll(uiConfig.getInputFields().getIgnored());
		}

		List<ModelField> results = new ArrayList<>();

		for (ModelField field : fields) {
			if (field.isOneToMany() || field.isManyToMany()) {
				continue;
			}
			if (!definedFields.isEmpty()) {
				if (!definedFields.contains(field.getName())) {
					continue;
				}
			}
			if (ignoredFields.contains(field.getName())) {
				continue;
			}

			if (field.isVersionField()) {
				field.setHidden(true);
			}

			results.add(field);
		}
		return results;
	}

	public static List<Field> getRelationFields(List<Field> fields) {
		return GeneratorUtils.getRelatedFields(fields, JoinTarget.JoinType.MANY_TO_ONE);
	}

	public static List<ModelField> getClobFields(List<ModelField> modelFields) {
		List<ModelField> results = new ArrayList<>();
		modelFields.forEach(modelField -> {
			if (StringUtils.equals("clob", modelField.getFieldType())) {
				results.add(modelField);
			}
		});
		return results;
	}
}
