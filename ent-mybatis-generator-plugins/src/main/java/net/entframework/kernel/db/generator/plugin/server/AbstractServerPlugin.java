/*
 * ******************************************************************************
 *  * Copyright (c) 2022. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */

package net.entframework.kernel.db.generator.plugin.server;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.plugin.AbstractDynamicSQLPlugin;
import net.entframework.kernel.db.generator.utils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.WriteMode;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.JoinEntry;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

/***
 * 增删改查，读取context配置参数
 */
public abstract class AbstractServerPlugin extends AbstractDynamicSQLPlugin {

	protected String pluginName = getClass().getName();

	protected List<GeneratedJavaFile> generatedJavaFiles = new ArrayList<>();

	protected String voTargetPackage;

	protected String voSuffix = "";

	protected String voRootClass = "";

	protected String mapstructTargetPackage;

	protected String mapstructSuffix = "";

	protected String serviceTargetPackage;

	protected String serviceSuffix = "";

	protected String baseServicePrefix = "";

	protected String servicePrefix = "";

	protected String repositoryTargetPackage;

	protected String repositorySuffix = "";

	protected String controllerTargetPackage;

	protected String controllerSuffix = "Controller";

	protected String controllerPrefix = "";

	protected String codingStyle;

	protected IntrospectedTable findParentTable(List<IntrospectedTable> tables, String parentTable) {
		return tables.stream()
			.filter(introspectedTable1 -> introspectedTable1.getFullyQualifiedTable()
				.getIntrospectedTableName()
				.equals(parentTable))
			.findFirst()
			.orElse(null);
	}

	@Override
	public boolean validate(List<String> warnings) {
		if (!"MyBatis3DynamicSql".equalsIgnoreCase(context.getTargetRuntime())) { //$NON-NLS-1$
			warnings.add("EntCrudPlugin 目前支持 runtime=MyBatis3DynamicSql"); //$NON-NLS-1$
			return false;
		}

		codingStyle = this.getProperty("generatedCodeStyle");
		if (StringUtils.isEmpty(codingStyle)) {
			codingStyle = Constants.GENERATED_CODE_STYLE;
		}

		this.voTargetPackage = this.getProperty("voTargetPackage");
		this.voSuffix = this.getProperty("voSuffix", Constants.DEFAULT_VO_SUFFIX);
		this.voRootClass = this.getProperty("voRootClass");

		this.mapstructTargetPackage = this.getProperty("mapstructTargetPackage");
		this.mapstructSuffix = this.getProperty("mapstructSuffix", Constants.DEFAULT_MAPSTRUCT_SUFFIX);

		this.serviceTargetPackage = this.getProperty("serviceTargetPackage");
		this.serviceSuffix = this.getProperty("serviceSuffix", Constants.DEFAULT_SERVICE_SUFFIX);
		this.baseServicePrefix = this.getProperty("baseServicePrefix", Constants.DEFAULT_BASE_SERVICE_PREFIX);
		this.servicePrefix = this.getProperty("servicePrefix", "");

		this.repositoryTargetPackage = this.getProperty("repositoryTargetPackage");
		this.repositorySuffix = this.getProperty("repositorySuffix", Constants.DEFAULT_REPOSITORY_SUFFIX);

		this.controllerTargetPackage = this.context.getProperty("controllerTargetPackage");
		this.controllerPrefix = this.getProperty("controllerPrefix", Constants.DEFAULT_BASE_CONTROLLER_PREFIX);

		String mode = this.properties.getProperty("writeMode");
		if (StringUtils.isNotEmpty(mode)) {
			WriteMode writeMode = convert(mode);
			if (writeMode != null) {
				this.writeMode = writeMode;
			}
			else {
				warnings.add(this.getClass().getName() + "配置了错误的WriteMode, 可用值: NEVER,OVER_WRITE,SKIP_ON_EXIST");
			}
		}

		return true;
	}

	// 先从plugin 配置获取，获取不到再从context获取
	protected String getProperty(String key, String defaultValue) {
		if (this.properties.containsKey(key) && StringUtility.stringHasValue(this.properties.getProperty(key))) {
			return this.properties.getProperty(key);
		}
		else if (StringUtility.stringHasValue(this.context.getProperty(key))) {
			return this.context.getProperty(key);
		}
		return defaultValue;
	}

	protected String getProperty(String key) {
		return getProperty(key, null);
	}

	public FullyQualifiedJavaType getMapperJavaType(String modelObjectName) {
		return new FullyQualifiedJavaType(this.context.getJavaClientGeneratorConfiguration().getTargetPackage() + "."
				+ modelObjectName + "Mapper");
	}

	public FullyQualifiedJavaType getVoJavaType(String modelObjectName) {
		return new FullyQualifiedJavaType(this.voTargetPackage + "." + modelObjectName + this.voSuffix);
	}

	public FullyQualifiedJavaType getJavaType(String modelObjectName, String targetPackage, String suffix) {
		return new FullyQualifiedJavaType(targetPackage + "." + modelObjectName + suffix);
	}

	public FullyQualifiedJavaType getMapstructJavaType(String modelObjectName) {
		return new FullyQualifiedJavaType(this.mapstructTargetPackage + "." + modelObjectName + this.mapstructSuffix);
	}

	public FullyQualifiedJavaType getBaseServiceJavaType(String modelObjectName) {
		return new FullyQualifiedJavaType(
				this.serviceTargetPackage + ".base." + this.baseServicePrefix + modelObjectName + this.serviceSuffix);
	}

	public FullyQualifiedJavaType getBaseServiceImplJavaType(String modelObjectName) {
		return new FullyQualifiedJavaType(this.serviceTargetPackage + ".base.impl." + this.baseServicePrefix
				+ modelObjectName + this.serviceSuffix + "Impl");
	}

	public FullyQualifiedJavaType getServiceJavaType(String modelObjectName) {
		return new FullyQualifiedJavaType(
				this.serviceTargetPackage + "." + this.servicePrefix + modelObjectName + this.serviceSuffix);
	}

	public FullyQualifiedJavaType getServiceImplJavaType(String modelObjectName) {
		return new FullyQualifiedJavaType(this.serviceTargetPackage + ".impl." + this.servicePrefix + modelObjectName
				+ this.serviceSuffix + "Impl");
	}

	public FullyQualifiedJavaType getRepositoryJavaType(String modelObjectName) {
		return new FullyQualifiedJavaType(this.repositoryTargetPackage + "." + modelObjectName + this.repositorySuffix);
	}

	public FullyQualifiedJavaType getRepositoryImplJavaType(String modelObjectName) {
		return new FullyQualifiedJavaType(
				this.repositoryTargetPackage + ".impl." + modelObjectName + this.repositorySuffix + "Impl");
	}

	public FullyQualifiedJavaType getBaseControllerJavaType(String modelObjectName) {
		return new FullyQualifiedJavaType(this.controllerTargetPackage + ".base." + this.controllerPrefix
				+ modelObjectName + this.controllerSuffix);
	}

	public FullyQualifiedJavaType getControllerJavaType(String modelObjectName) {
		return new FullyQualifiedJavaType(this.controllerTargetPackage + "." + modelObjectName + this.controllerSuffix);
	}

	public boolean isManyToManyMiddleTable(IntrospectedTable table) {
		String tableName = table.getFullyQualifiedTableNameAtRuntime();

		for (JoinEntry joinEntry : this.context.getJoinConfig().getJoinDetailMap().values()) {
			if (joinEntry.getJoinTables()
				.stream()
				.anyMatch(joinTable -> StringUtils.equals(joinTable.getMiddleTable(), tableName))) {
				return true;
			}
		}
		return false;
	}

}
