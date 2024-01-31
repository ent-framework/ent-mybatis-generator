/*
 * ******************************************************************************
 *  * Copyright (c) 2022. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */

package net.entframework.kernel.db.generator.plugin.server;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.plugin.generator.FieldAndImports;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import net.entframework.kernel.db.generator.plugin.generator.VoFieldsGenerator;
import net.entframework.kernel.db.generator.utils.ClassInfo;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.WriteMode;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.List;

/***
 * Vo生成
 */
public class VoPlugin extends AbstractServerPlugin {

	@Override
	public boolean validate(List<String> warnings) {

		boolean validate = super.validate(warnings);

		if (StringUtils.isAnyEmpty(this.voTargetPackage, this.voSuffix, this.mapstructTargetPackage,
				this.mapstructSuffix)) {
			warnings.add("请检查PojoPlugin配置");
			return false;
		}

		return validate;
	}

	/***
	 * 在model产生后新增pojo request 和 pojo response 插件注册时要注意顺序，因为需要从TopLevelClass读取所有Field
	 * @param introspectedTable The class containing information about the table as
	 * introspected from the database
	 * @return
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(IntrospectedTable introspectedTable) {
		TopLevelClass topLevelClass = introspectedTable.getBaseModelClass();
		FullyQualifiedJavaType qualifiedJavaType = topLevelClass.getType();
		VoFieldsGenerator pojoFieldsGenerator = new VoFieldsGenerator(this.context, this.codingStyle,
				this.voTargetPackage, this.voSuffix, qualifiedJavaType);

		// 判断是否包含Entity父类
		String rootClass = this.context.getJavaModelGeneratorConfiguration().getProperty("rootClass");
		if (StringUtils.isNotEmpty(rootClass)) {
			ClassInfo classInfo = ClassInfo.getInstance(rootClass);
			TopLevelClass parentEntityClass = classInfo.toTopLevelClass();
			if (parentEntityClass != null) {
				topLevelClass.setAttribute(Constants.PARENT_ENTITY_CLASS, parentEntityClass);
			}
		}
		generatedJavaFiles.add(generateVO(topLevelClass, introspectedTable, pojoFieldsGenerator));

		return true;
	}

	private GeneratedJavaFile generateVO(TopLevelClass topLevelClass, IntrospectedTable introspectedTable,
			VoFieldsGenerator pojoFieldsGenerator) {
		String modelObjectName = topLevelClass.getType().getShortNameWithoutTypeArguments();

		FullyQualifiedJavaType voJavaType = getVoJavaType(modelObjectName);
		TopLevelClass voClass = new TopLevelClass(voJavaType);
		voClass.setVisibility(JavaVisibility.PUBLIC);

		boolean voParentTableFound = false;
		if (StringUtils.isNotEmpty(introspectedTable.getTableConfiguration().getParentTable())) {
			IntrospectedTable parentTable = findParentTable(this.context.getIntrospectedTables(),
					introspectedTable.getTableConfiguration().getParentTable());
			if (parentTable != null) {
				voParentTableFound = true;
				FullyQualifiedJavaType parent = getVoJavaType(
						parentTable.getFullyQualifiedTable().getDomainObjectName());
				voClass.setSuperClass(parent);
			}
		}

		if (StringUtils.isNotEmpty(this.voRootClass)) {
			if (!voParentTableFound) {
				voClass.setSuperClass(this.voRootClass);
				voClass.addImportedType(this.voRootClass);
			}
			ClassInfo classInfo = ClassInfo.getInstance(this.voRootClass);
			TopLevelClass parentRequestClass = classInfo.toTopLevelClass();
			if (parentRequestClass != null) {
				voClass.setAttribute(Constants.PARENT_REQUEST_CLASS, parentRequestClass);
			}
		}

		voClass.setWriteMode(this.writeMode == null ? WriteMode.OVER_WRITE : this.writeMode);

		GeneratorUtils.addComment(voClass, topLevelClass.getDescription() + " VO类");

		addLombokAnnotation(voClass);

		voClass.addAnnotation(String.format("@Schema(description = \"%s\")", topLevelClass.getDescription()));
		voClass.addImportedType("io.swagger.v3.oas.annotations.media.Schema");

		FieldAndImports fieldAndImports = pojoFieldsGenerator.generateVo(topLevelClass, introspectedTable, true);

		fieldAndImports.getFields().forEach(voClass::addField);
		fieldAndImports.getImports().forEach(voClass::addImportedType);

		GeneratedJavaFile generatedJavaFile = new GeneratedJavaFile(voClass,
				context.getJavaModelGeneratorConfiguration().getTargetProject(),
				context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
		generatedJavaFile.setOutputDirectory(getOutputDirectory());
		return generatedJavaFile;
	}

	private void addLombokAnnotation(TopLevelClass topLevelClass) {
		topLevelClass.addImportedType(LombokAnnotation.DATA.getJavaType());
		topLevelClass.addAnnotation(LombokAnnotation.DATA.getName());
		topLevelClass.addImportedType(LombokAnnotation.EqualsAndHashCode.getJavaType());
		topLevelClass.addAnnotation(LombokAnnotation.EqualsAndHashCode.getName());
		topLevelClass.addImportedType(LombokAnnotation.ALL_ARGS_CONSTRUCTOR.getJavaType());
		topLevelClass.addAnnotation(LombokAnnotation.ALL_ARGS_CONSTRUCTOR.getName());
		topLevelClass.addImportedType(LombokAnnotation.NO_ARGS_CONSTRUCTOR.getJavaType());
		topLevelClass.addAnnotation(LombokAnnotation.NO_ARGS_CONSTRUCTOR.getName());
	}

	/**
	 * @return
	 */
	@Override
	public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
		return generatedJavaFiles;
	}

}
