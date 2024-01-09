/*
 * ******************************************************************************
 *  * Copyright (c) 2022-2023. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */

package net.entframework.kernel.db.generator.plugin.server;

import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.WriteMode;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.List;

/***
 * Service生成
 */
public class ServicePlugin extends AbstractServerPlugin {

	@Override
	public boolean validate(List<String> warnings) {

		boolean validate = super.validate(warnings);

		if (StringUtils.isAnyEmpty(this.serviceTargetPackage, this.serviceSuffix)) {
			warnings.add("请检查ServicePlugin配置");
			return false;
		}

		return validate;
	}

	/**
	 * Mapper 文件生成时，同步生成service接口及实现
	 * @param interfaze the generated interface if any, may be null
	 * @param introspectedTable The class containing information about the table as
	 * introspected from the database
	 * @return true/false
	 */
	public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {

		boolean hasGeneratedKeys = introspectedTable.getGeneratedKey().isPresent();
		generatedJavaFiles.add(generateBaseServiceInterface(interfaze, introspectedTable, hasGeneratedKeys));
		generatedJavaFiles.add(generateBaseServiceInterfaceImpl(interfaze, introspectedTable, hasGeneratedKeys));

		return true;
	}

	private GeneratedJavaFile generateBaseServiceInterface(Interface clientInterface,
			IntrospectedTable introspectedTable, boolean hasGeneratedKeys) {
		String baseRecordType = introspectedTable.getBaseRecordType();
		FullyQualifiedJavaType recordType = new FullyQualifiedJavaType(baseRecordType);
		FullyQualifiedJavaType baseServiceJavaType = getServiceJavaType(recordType.getShortName());

		Interface serviceInterface = new Interface(baseServiceJavaType);
		serviceInterface.setVisibility(JavaVisibility.PUBLIC);

		serviceInterface.setWriteMode(this.writeMode == null ? WriteMode.SKIP_ON_EXIST : this.writeMode);
		IntrospectedColumn pkColumn = GeneratorUtils.getPrimaryKey(introspectedTable);
		FullyQualifiedJavaType baseServiceType = new FullyQualifiedJavaType(
				"net.entframework.kernel.db.dao.service.BaseService");
		baseServiceType.addTypeArgument(recordType);
		baseServiceType.addTypeArgument(pkColumn.getFullyQualifiedJavaType());
		serviceInterface.addSuperInterface(baseServiceType);

		serviceInterface.addImportedType(baseServiceType);

		GeneratedJavaFile gjf = new GeneratedJavaFile(serviceInterface,
				context.getJavaModelGeneratorConfiguration().getTargetProject(),
				context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
		gjf.setOutputDirectory(getOutputDirectory());
		return gjf;
	}

	private GeneratedJavaFile generateBaseServiceInterfaceImpl(Interface clientInterface,
			IntrospectedTable introspectedTable, boolean hasGeneratedKeys) {
		String baseRecordType = introspectedTable.getBaseRecordType();
		FullyQualifiedJavaType recordType = new FullyQualifiedJavaType(baseRecordType);
		FullyQualifiedJavaType baseRepositoryJavaType = getRepositoryJavaType(recordType.getShortName());
		FullyQualifiedJavaType serviceImplJavaType = getServiceImplJavaType(recordType.getShortName());
		TopLevelClass serviceInterfaceImplClass = new TopLevelClass(serviceImplJavaType);
		serviceInterfaceImplClass.setVisibility(JavaVisibility.PUBLIC);
		serviceInterfaceImplClass.setAbstract(false);
		FullyQualifiedJavaType interfaceType = getServiceJavaType(recordType.getShortName());
		serviceInterfaceImplClass.setWriteMode(this.writeMode == null ? WriteMode.SKIP_ON_EXIST : this.writeMode);

		serviceInterfaceImplClass.addAnnotation(LombokAnnotation.SLF4J.getName());
		serviceInterfaceImplClass.addImportedType(LombokAnnotation.SLF4J.getJavaType());

		serviceInterfaceImplClass.addSuperInterface(interfaceType);
		serviceInterfaceImplClass.addImportedType(interfaceType);
		IntrospectedColumn pkColumn = GeneratorUtils.getPrimaryKey(introspectedTable);
		FullyQualifiedJavaType baseServiceType = new FullyQualifiedJavaType(
				"net.entframework.kernel.db.dao.service.BaseServiceImpl");
		baseServiceType.addTypeArgument(recordType);
		baseServiceType.addTypeArgument(pkColumn.getFullyQualifiedJavaType());
		serviceInterfaceImplClass.setSuperClass(baseServiceType);

		serviceInterfaceImplClass.addImportedType(baseServiceType);
		Parameter p1 = new Parameter(baseRepositoryJavaType,
				StringUtils.uncapitalize(baseRepositoryJavaType.getShortName()));
		// 构造器
		Method defaultConstructor = new Method(serviceImplJavaType.getShortName());
		defaultConstructor.setConstructor(true);
		defaultConstructor.setVisibility(JavaVisibility.PUBLIC);
		defaultConstructor.addParameter(p1);
		defaultConstructor.addBodyLine(String.format("super(%s, %s.class);", p1.getName(), recordType.getShortName()));

		Method method = new Method(serviceImplJavaType.getShortName());
		FullyQualifiedJavaType entityClsJavaType = new FullyQualifiedJavaType(
				String.format("Class<? extends %s>", recordType.getShortName()));

		method.addParameter(p1);
		serviceInterfaceImplClass.addImportedType(baseRepositoryJavaType);

		Parameter p2 = new Parameter(entityClsJavaType, "entityClass");
		method.addParameter(p2);
		method.setConstructor(true);
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addBodyLine(String.format("super(%s, entityClass);", p1.getName()));

		serviceInterfaceImplClass.addMethod(defaultConstructor);
		serviceInterfaceImplClass.addMethod(method);

		GeneratedJavaFile gjf = new GeneratedJavaFile(serviceInterfaceImplClass,
				context.getJavaModelGeneratorConfiguration().getTargetProject(),
				context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
		gjf.setOutputDirectory(getOutputDirectory());
		return gjf;
	}

	/**
	 * @return
	 */
	@Override
	public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
		return generatedJavaFiles;
	}

}
