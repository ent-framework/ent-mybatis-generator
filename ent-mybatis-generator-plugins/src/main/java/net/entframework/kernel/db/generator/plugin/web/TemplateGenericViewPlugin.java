package net.entframework.kernel.db.generator.plugin.web;

import net.entframework.kernel.db.generator.typescript.runtime.FullyQualifiedTypescriptType;
import net.entframework.kernel.db.generator.typescript.runtime.ModelObject;
import net.entframework.kernel.db.generator.typescript.runtime.TemplateGeneratedFile;
import net.entframework.kernel.db.generator.typescript.runtime.TypescriptTopLevelClass;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.WriteMode;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 所有数据数据模型构建完成后 通常根据所有的数据模型，生成路由信息等操作
 */
public class TemplateGenericViewPlugin extends AbstractTemplatePlugin {

	private GeneratedFile generateModelClass() {

		FullyQualifiedTypescriptType tsBaseModelJavaType = new FullyQualifiedTypescriptType(
				this.targetPackage + "." + this.fileName, true);
		TypescriptTopLevelClass tsGenericClass = new TypescriptTopLevelClass(tsBaseModelJavaType);

		tsGenericClass.setWriteMode(this.writeMode == null ? WriteMode.SKIP_ON_EXIST : this.writeMode);

		Map<String, Object> data = new HashMap<>();
		data.put("projectRootAlias", this.projectRootAlias);
		data.put("modelPath", this.modelPath);
		String apiPath = StringUtils.replace(this.apiPackage, ".", "/");
		data.put("apiPath", apiPath);
		data.put("viewPath", StringUtils.replace(this.viewPackage, ".", "/"));
		data.putAll(getAdditionalPropertyMap());

		List<ModelObject> modelObjects = new ArrayList<>();

		List<IntrospectedTable> tables = this.context.getIntrospectedTables();

		for (IntrospectedTable table : tables) {
			TopLevelClass topLevelClass = table.getBaseModelClass();
			if (topLevelClass != null) {
				ModelObject.Builder builder = ModelObject.builder();
				String modelObjectName = topLevelClass.getType().getShortName();
				builder.name(modelObjectName)
					.camelModelName(JavaBeansUtil.convertCamelCase(modelObjectName, "-"))
					.description(StringUtils.isEmpty(topLevelClass.getDescription())
							? table.getFullyQualifiedTable().getDomainObjectName() : topLevelClass.getDescription())
					.type(modelObjectName);
				modelObjects.add(builder.build());
			}
		}

		data.put("models", modelObjects);

		return new TemplateGeneratedFile(tsGenericClass,
				context.getJavaModelGeneratorConfiguration().getTargetProject(), data, this.templatePath, this.fileName,
				this.fileExt);
	}

	@Override
	public List<GeneratedFile> contextGenerateAdditionalFiles() {
		List<GeneratedFile> generatedFiles = new ArrayList<>();
		generatedFiles.add(generateModelClass());
		return generatedFiles;
	}

}
