package net.entframework.kernel.db.generator.plugin.web;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.config.Relation;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import net.entframework.kernel.db.generator.typescript.runtime.FullyQualifiedTypescriptType;
import net.entframework.kernel.db.generator.typescript.runtime.ModelField;
import net.entframework.kernel.db.generator.typescript.runtime.TemplateGeneratedFile;
import net.entframework.kernel.db.generator.typescript.runtime.TypescriptTopLevelClass;
import net.entframework.kernel.db.generator.utils.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.WriteMode;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.JoinTarget;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 * 数据模型构建后都会执行 根据数据模型生成对应的ts,vue文件
 */
public class TemplateModelViewPlugin extends AbstractTemplatePlugin {

	private final List<GeneratedFile> generatedFiles = new ArrayList<>();

	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

		generatedFiles.add(generateModelClass(topLevelClass, introspectedTable));

		return true;
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

		tsApiClass.setWriteMode(this.writeMode == null ? WriteMode.SKIP_ON_EXIST : this.writeMode);

		Map<String, Object> data = new HashMap<>();
		data.put("projectRootAlias", this.projectRootAlias);
		data.put("modelPackage", this.typescriptModelPackage.replaceAll("\\.", "/"));

		data.put("modelName", modelObjectName);
		data.put("modelDescription", topLevelClass.getDescription());
		data.put("camelModelName", camelCaseName);
		data.put("modelPath", this.modelPath);

		data.putAll(getAdditionalPropertyMap());

		IntrospectedColumn pkColumn = GeneratorUtils.getPrimaryKey(introspectedTable);
		List<Field> fields = WebUtils.getFieldsWithoutPrimaryKey(topLevelClass.getFields(), pkColumn.getJavaProperty());
		data.put("fields", fields);
		data.put("listFields", convert(WebUtils.getListFields(fields, getListIgnoreFields()), introspectedTable));
		data.put("searchFields", convert(WebUtils.getSearchFields(fields, getListIgnoreFields()), introspectedTable));
		data.put("inputFields", convert(WebUtils.getInputFields(fields, getInputIgnoreFields()), introspectedTable));

		// 获取枚举字段
		List<Field> enumFields = fields.stream()
			.filter(field -> field.getAttribute(Constants.TABLE_ENUM_FIELD_ATTR) != null
					&& !GeneratorUtils.isLogicDeleteField(field))
			.collect(Collectors.toSet())
			.stream()
			.toList();
		data.put("enumFields", convert(enumFields, introspectedTable));
		data.put("relationFields", convert(WebUtils.getRelationFields(fields), introspectedTable));
		data.put("pk",
				new ModelField(GeneratorUtils.getFieldByName(topLevelClass, pkColumn.getJavaProperty()), pkColumn));

		String apiPath = StringUtils.replace(this.apiPackage, ".", "/");
		data.put("apiPath", apiPath);
		data.put("viewPath", StringUtils.replace(this.viewPackage, ".", "/"));

		return new TemplateGeneratedFile(tsApiClass, context.getJavaModelGeneratorConfiguration().getTargetProject(),
				data, this.templatePath, this.fileName, this.fileExt);
	}

	public List<GeneratedFile> contextGenerateAdditionalFiles() {
		return generatedFiles;
	}

	protected List<ModelField> convert(List<Field> fields, IntrospectedTable introspectedTable) {
		List<ModelField> modelFields = new ArrayList<>();
		for (Field field : fields) {
			IntrospectedColumn column = null;
			if (GeneratorUtils.isRelationField(introspectedTable, field)) {
				Relation relation = (Relation) field.getAttribute(Constants.FIELD_RELATION);
				if (relation.getJoinType() == JoinTarget.JoinType.MANY_TO_ONE) {
					column = GeneratorUtils.safeGetIntrospectedColumnByJavaProperty(introspectedTable,
							relation.getSourceField().getName());
				}
				else {
					continue;
				}
			}
			else {
				column = GeneratorUtils.safeGetIntrospectedColumnByJavaProperty(introspectedTable, field.getName());
			}
			if (column == null) {
				log.warn("Can't find column in table: "
						+ introspectedTable.getFullyQualifiedTable().getIntrospectedTableName() + " by field name: "
						+ field.getName() + "");
				continue;
			}
			ModelField modelField = new ModelField(field, column);
			modelFields.add(modelField);
		}
		return modelFields;
	}

}
