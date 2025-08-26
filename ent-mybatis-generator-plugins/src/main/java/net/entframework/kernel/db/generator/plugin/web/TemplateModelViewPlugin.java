package net.entframework.kernel.db.generator.plugin.web;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import net.entframework.kernel.db.generator.typescript.runtime.*;
import net.entframework.kernel.db.generator.utils.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.WriteMode;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.*;
import java.util.stream.Collectors;

/***
 * 数据模型构建后都会执行 根据数据模型生成对应的ts,vue文件
 */
public class TemplateModelViewPlugin extends AbstractTemplatePlugin {

	@Override
	public List<GeneratedFile> contextGenerateAdditionalFiles(IntrospectedTable introspectedTable) {
		TopLevelClass topLevelClass = introspectedTable.getBaseModelClass();
		List<GeneratedFile> results = new ArrayList<>();
		results.add(generateModelClass(topLevelClass, introspectedTable));
		return results;
	}

	private GeneratedFile generateModelClass(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		String modelObjectName = topLevelClass.getType().getShortNameWithoutTypeArguments();
		String camelCaseName = JavaBeansUtil.convertCamelCase(modelObjectName, "-");
		FullyQualifiedTypescriptType tsBaseModelJavaType;
		if (this.enableSubPackages) {
			tsBaseModelJavaType = new FullyQualifiedTypescriptType(
					this.targetPackage + "." + camelCaseName + "." + modelObjectName, true);
		}
		else {
			tsBaseModelJavaType = new FullyQualifiedTypescriptType(this.targetPackage + "." + modelObjectName, true);
		}
		TypescriptTopLevelClass tsApiClass = new TypescriptTopLevelClass(tsBaseModelJavaType);

		ModelObject.Builder builder = ModelObject.builder();
		builder.name(modelObjectName)
			.camelName(JavaBeansUtil.convertCamelCase(modelObjectName, "-"))
			.description(StringUtils.isEmpty(topLevelClass.getDescription())
					? introspectedTable.getFullyQualifiedTable().getDomainObjectName() : topLevelClass.getDescription())
			.type(modelObjectName);
		ModelObject modelObject = builder.build();

		tsApiClass.setWriteMode(this.writeMode == null ? WriteMode.SKIP_ON_EXIST : this.writeMode);

		Map<String, Object> data = new HashMap<>();
		data.put("projectRootAlias", this.projectRootAlias);

		modelObject.setPath(this.modelPath);
		modelObject.setModelPackage(this.typescriptModelPackage.replaceAll("\\.", "/"));

		data.putAll(getAdditionalPropertyMap());

		IntrospectedColumn pkColumn = GeneratorUtils.getPrimaryKey(introspectedTable);
		List<Field> fields = topLevelClass.getFields();
		List<ModelField> modelFields = convert(fields, introspectedTable, pkColumn);

		modelFields.stream().filter(ModelField::isVersionField).findFirst().ifPresent(mf -> {
			modelObject.setVersionField(mf.getName());
		});

		// 获取枚举字段
		List<ModelField> enumFields = modelFields.stream()
			.filter(mf -> mf.isEnumField() && !mf.isLogicDeleteField())
			.collect(Collectors.toSet())
			.stream()
			.toList();
		// 关系字段
		List<ModelField> relationFields = modelFields.stream()
			.filter(ModelField::isRelationField)
			.collect(Collectors.toSet())
			.stream()
			.toList();
		// 生成描述Detail Schema配置
		data.put("detailFields", WebUtils.getDetailFields(copy(modelFields)));

		Set<String> listIgnoreFields = getListIgnoreFields();
		relationFields.forEach(mf -> {
			if (mf.isManyToMany()) {
				listIgnoreFields.add(mf.getName());
			}
			else if (mf.isOneToMany() || mf.isManyToOne()) {
				listIgnoreFields.add(mf.getRelation().getSourceField().getName());
			}
		});

		data.put("clobFields", WebUtils.getClobFields(modelFields));
		List<ModelField> listFields = WebUtils.getListFields(copy(modelFields), listIgnoreFields, introspectedTable);

		if (!modelObject.isTenant()) {
			modelObject.setTenant(listFields.stream().anyMatch(ModelField::isTenantField));
		}

		if (listFields.stream().anyMatch(ModelField::isEnumLabel)) {
			modelObject.setEnumLabel(true);
		}
		if (listFields.stream().anyMatch(ModelField::isEnumSwitch)) {
			modelObject.setEnumSwitch(true);
		}
		List<ModelField> searchFields = WebUtils.getSearchFields(copy(modelFields), introspectedTable);

		if (!modelObject.isTenant()) {
			modelObject.setTenant(searchFields.stream().anyMatch(ModelField::isTenantField));
		}

		data.put("listFields", listFields);
		data.put("searchFields", searchFields);
		//
		Set<String> inputIgnoreFields = getInputIgnoreFields();
		relationFields.forEach(mf -> {
			if (mf.isManyToMany()) {
				inputIgnoreFields.add(mf.getName());
			}
			else if (mf.isOneToMany() || mf.isManyToOne()) {
				inputIgnoreFields.add(mf.getRelation().getSourceField().getName());
			}
		});
		inputIgnoreFields.add(pkColumn.getJavaProperty());
		List<ModelField> inputFields = WebUtils.getInputFields(copy(modelFields), inputIgnoreFields, introspectedTable);
		data.put("inputFields", inputFields);

		if (!modelObject.isTenant()) {
			modelObject.setTenant(inputFields.stream().anyMatch(ModelField::isTenantField));
		}
		Set<FullyQualifiedJavaType> enumFieldImport = enumFields.stream()
			.map(ModelField::getJavaType)
			.collect(Collectors.toSet());
		data.put("enumFields", enumFields);
		data.put("enumFieldImport", enumFieldImport);
		data.put("relationFields", relationFields);
		data.put("pk",
				new ModelField(GeneratorUtils.getFieldByName(topLevelClass, pkColumn.getJavaProperty()), pkColumn));

		String apiPath = StringUtils.replace(this.apiPackage, ".", "/");
		data.put("apiPath", apiPath);
		data.put("viewPath", StringUtils.replace(this.viewPackage, ".", "/"));
		data.put("model", modelObject);
		return new TemplateGeneratedFile(tsApiClass, context.getJavaModelGeneratorConfiguration().getTargetProject(),
				data, this.templatePath, this.fileName, this.fileExt);
	}

	/**
	 * 字段转换，只保留many-to-one类型的
	 * @param fields
	 * @param introspectedTable
	 * @return
	 */
	private List<ModelField> convert(List<Field> fields, IntrospectedTable introspectedTable,
			IntrospectedColumn pkColumn) {
		List<ModelField> modelFields = new ArrayList<>();
		for (Field field : fields) {
			IntrospectedColumn column = null;
			if (GeneratorUtils.isRelationField(introspectedTable, field)) {
				column = (IntrospectedColumn) field.getAttribute(Constants.FIELD_RELATION_COLUMN);
			}
			else {
				column = GeneratorUtils.safeGetIntrospectedColumnByJavaProperty(introspectedTable, field.getName());
			}
			if (column == null) {
				throw new RuntimeException("Can't find column in table: "
						+ introspectedTable.getFullyQualifiedTable().getIntrospectedTableName() + " by field name: "
						+ field.getName());
			}
			ModelField modelField = new ModelField(field, column);
			if (StringUtils.equals(column.getActualColumnName(), pkColumn.getActualColumnName())) {
				modelField.setPrimaryKey(true);
			}
			modelFields.add(modelField);
		}
		return modelFields;
	}

	private List<ModelField> copy(List<ModelField> fields) {
		return fields.stream().map(ModelField::copy).toList();
	}

}
