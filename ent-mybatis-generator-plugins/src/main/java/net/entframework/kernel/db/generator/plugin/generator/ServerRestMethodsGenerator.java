/*
 * ******************************************************************************
 *  * Copyright (c) 2022-2023. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */

package net.entframework.kernel.db.generator.plugin.generator;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Parameter;

public class ServerRestMethodsGenerator {

	private final RestMethodAndImports.Builder builder = new RestMethodAndImports.Builder();

	private final FullyQualifiedJavaType recordType;

	private final FullyQualifiedJavaType voJavaType;

	private final String serviceFieldName;

	private FullyQualifiedJavaType baseVoType;

	private final IntrospectedColumn pkColumn;

	private final boolean addAnnotation;

	public ServerRestMethodsGenerator(FullyQualifiedJavaType recordType, FullyQualifiedJavaType voJavaType,
			String serviceFieldName, IntrospectedColumn pkColumn, boolean addAnnotation) {
		this.recordType = recordType;
		this.voJavaType = voJavaType;
		this.serviceFieldName = serviceFieldName;
		this.pkColumn = pkColumn;
		this.addAnnotation = addAnnotation;
	}

	public RestMethodAndImports generate() {
		// 新增
		addCreateMethod();
		// 批量新增
		// addBatchCreateMethod();
		// 更新
		addUpdateMethod();
		// 列表查询
		addQueryListMethod();
		// 分页查询
		addPageListMethod();
		// 根据主键ID 删除
		addDeleteByPrimaryKeyMethod();
		// 批量删除
		addBatchDeleteMethod();
		// 获取单条记录
		addSelectByPrimaryKeyMethod();

		builder.withImport(recordType);

		if (this.baseVoType != null) {
			builder.withImport(this.baseVoType);
		}

		return builder.build();
	}

	public void setBaseVoType(FullyQualifiedJavaType baseRequestType) {
		this.baseVoType = baseRequestType;
	}

	public RestMethodAndImports build() {
		return builder.build();
	}

	public RestMethodAndImports.Builder getBuilder() {
		return builder;
	}

	public void addCreateMethod() {
		RestMethod method = new RestMethod("insert", "POST", recordType);
		method.setUrl("/create");
		method.setOperation("新增");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(voJavaType);
		builder.withImport(voJavaType);

		Parameter parameter = new Parameter(voJavaType, "vo");
		if (addAnnotation) {
			parameter.addAnnotation("@RequestBody");
			parameter.addAnnotation(String.format("@Validated(%s.add.class)",
					baseVoType == null ? voJavaType.getShortName() : baseVoType.getShortName()));
			builder.withImport("org.springframework.web.bind.annotation.RequestBody");
			builder.withImport("org.springframework.validation.annotation.Validated");
		}

		method.addBodyLine(String.format("%s record = converterService.convert(vo, %s.class);",
				recordType.getShortName(), recordType.getShortName()));
		method.addBodyLine(String.format("%s result = converterService.convert(%s.insert(record), %s.class);",
				voJavaType.getShortName(), serviceFieldName, voJavaType.getShortName()));
		method.addParameter(parameter);

		builder.withMethod(method);
	}

	public void addBatchCreateMethod() {
		RestMethod method = new RestMethod("insertMultiple", "POST", recordType);
		method.setUrl("/batch-create");
		method.setOperation("批量新增");
		method.setVisibility(JavaVisibility.PUBLIC);

		method.setReturnType(voJavaType);
		builder.withImport(voJavaType);

		FullyQualifiedJavaType responseBodyWrapperListType = FullyQualifiedJavaType.getNewListInstance();
		responseBodyWrapperListType.addTypeArgument(voJavaType);

		method.setReturnType(responseBodyWrapperListType);

		FullyQualifiedJavaType paramListType = FullyQualifiedJavaType.getNewListInstance();
		paramListType.addTypeArgument(voJavaType);
		builder.withImport(paramListType);
		Parameter parameter = new Parameter(paramListType, "voList");
		if (addAnnotation) {
			parameter.addAnnotation("@RequestBody");
			builder.withImport("org.springframework.web.bind.annotation.RequestBody");
		}
		method.addBodyLine(String.format("List<%s> records = converterService.convert(voList, %s.class);",
				recordType.getShortName(), recordType.getShortName()));
		method.addBodyLine(
				String.format("List<%s> result = converterService.convert(%s.insertMultiple(records), %s.class);",
						voJavaType.getShortName(), serviceFieldName, voJavaType.getShortName()));
		method.addParameter(parameter);

		builder.withMethod(method);
	}

	public void addUpdateMethod() {
		RestMethod method = new RestMethod("update", "POST", recordType);
		method.setOperation("更新-by PK");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(voJavaType);
		Parameter parameter = new Parameter(voJavaType, "vo");
		if (addAnnotation) {
			parameter.addAnnotation("@RequestBody");
			parameter.addAnnotation(String.format("@Validated(%s.update.class)",
					baseVoType == null ? voJavaType.getShortName() : baseVoType.getShortName()));
		}
		method.addBodyLine(String.format("%s record = converterService.convert(vo, %s.class);",
				recordType.getShortName(), recordType.getShortName()));
		method.addBodyLine(String.format("%s result = converterService.convert(%s.update(record), %s.class);",
				voJavaType.getShortName(), serviceFieldName, voJavaType.getShortName()));

		method.addParameter(parameter);
		builder.withMethod(method);
	}

	public void addDeleteByPrimaryKeyMethod() {
		RestMethod method = new RestMethod("delete", "POST", recordType);
		method.setUrl("/delete");
		method.setOperation("删除-by PK");
		method.setVisibility(JavaVisibility.PUBLIC);
		FullyQualifiedJavaType responseJavaType = new FullyQualifiedJavaType("Integer");
		method.setReturnType(responseJavaType);
		Parameter parameter = new Parameter(voJavaType, "vo");
		if (addAnnotation) {
			parameter.addAnnotation("@RequestBody");
			parameter.addAnnotation(String.format("@Validated(%s.delete.class)",
					baseVoType == null ? voJavaType.getShortName() : baseVoType.getShortName()));
		}
		method.addBodyLine(String.format("%s record = converterService.convert(vo, %s.class);",
				recordType.getShortName(), recordType.getShortName()));
		method.addBodyLine(String.format("Integer result = %s.delete(record);", serviceFieldName));

		method.addParameter(parameter);

		builder.withMethod(method);
	}

	public void addBatchDeleteMethod() {
		RestMethod method = new RestMethod("batchDelete", "POST", recordType);
		method.setOperation("批量删除-by PK");
		method.setVisibility(JavaVisibility.PUBLIC);
		FullyQualifiedJavaType responseJavaType = new FullyQualifiedJavaType("Integer");

		FullyQualifiedJavaType responseBodyWrapperListType = FullyQualifiedJavaType.getNewListInstance();
		responseBodyWrapperListType.addTypeArgument(voJavaType);

		method.setReturnType(responseJavaType);
		Parameter parameter = new Parameter(responseBodyWrapperListType, "voList");
		if (addAnnotation) {
			parameter.addAnnotation("@RequestBody");
			parameter.addAnnotation(String.format("@Validated(%s.batchDelete.class)",
					baseVoType == null ? voJavaType.getShortName() : baseVoType.getShortName()));
		}
		method.addParameter(parameter);
		method.addBodyLine(String.format("List<%s> record = converterService.convert(voList, %s.class);",
				recordType.getShortName(), recordType.getShortName()));
		method.addBodyLine(String.format("Integer result = %s.batchDelete(record);", serviceFieldName));

		builder.withMethod(method);
	}

	public void addQueryListMethod() {
		RestMethod method = new RestMethod("list", "GET", recordType);
		method.setOperation("列表");
		method.setVisibility(JavaVisibility.PUBLIC);
		FullyQualifiedJavaType responseBodyWrapperListType = FullyQualifiedJavaType.getNewListInstance();
		responseBodyWrapperListType.addTypeArgument(voJavaType);

		method.setReturnType(responseBodyWrapperListType);
		builder.withImport(responseBodyWrapperListType);
		Parameter parameter = new Parameter(voJavaType, "vo");
		method.addParameter(parameter);

		method.addParameter(getRequestParam());

		method.addBodyLine(String.format("%s query = converterService.convert(vo, %s.class);",
				recordType.getShortName(), recordType.getShortName()));
		method.addBodyLine(String.format(
				"List<%s> result = converterService.convert(%s.select(query, BaseQuery.from(request)), %s.class);",
				voJavaType.getShortName(), serviceFieldName, voJavaType.getShortName()));

		builder.withImport("net.entframework.kernel.core.vo.BaseQuery");

		builder.withMethod(method);
	}

	private Parameter getRequestParam() {
		FullyQualifiedJavaType requestJavaType = new FullyQualifiedJavaType("jakarta.servlet.http.HttpServletRequest");
		builder.withImport(requestJavaType);
		return new Parameter(requestJavaType, "request");
	}

	public void addPageListMethod() {
		RestMethod method = new RestMethod("page", "GET", recordType);
		method.setOperation("分页查询");
		method.setVisibility(JavaVisibility.PUBLIC);
		FullyQualifiedJavaType pageResultType = new FullyQualifiedJavaType(
				"net.entframework.kernel.db.api.pojo.page.PageResult");
		builder.withImport(pageResultType);
		pageResultType.addTypeArgument(voJavaType);
		method.setReturnType(pageResultType);

		Parameter parameter = new Parameter(voJavaType, "vo");
		method.addParameter(parameter);
		method.addParameter(getRequestParam());

		method.addBodyLine(String.format("%s record = converterService.convert(vo, %s.class);",
				recordType.getShortName(), recordType.getShortName()));
		method.addBodyLine(String.format("PageResult<%s> page = %s.page(record, BaseQuery.from(request));",
				recordType.getShortName(), serviceFieldName));
		method.addBodyLine(String.format("List<%s> records = converterService.convert(page.getItems(), %s.class);",
				voJavaType.getShortName(), voJavaType.getShortName()));
		method.addBodyLine(String.format(
				"PageResult<%s> result =  PageResultFactory.createPageResult(records, (long)page.getTotalRows(), page.getPageSize(), page.getPageNo());",
				voJavaType.getShortName()));
		builder.withImport("net.entframework.kernel.core.vo.BaseQuery");
		builder.withImport("net.entframework.kernel.db.api.factory.PageResultFactory");
		builder.withMethod(method);
	}

	public void addSelectByPrimaryKeyMethod() {
		RestMethod method = new RestMethod("load", "GET", recordType);
		method.setUrl("/detail");
		method.setOperation("获取记录-by PK");
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(voJavaType);
		Parameter parameter = new Parameter(voJavaType, "vo");
		if (addAnnotation) {
			parameter.addAnnotation(String.format("@Validated(%s.detail.class)",
					baseVoType == null ? voJavaType.getShortName() : baseVoType.getShortName()));
		}
		method.addParameter(parameter);
		String pk = StringUtils.capitalize(pkColumn.getJavaProperty());
		method.addBodyLine(String.format("%s result = converterService.convert(%s.load(vo.get%s()), %s.class);",
				voJavaType.getShortName(), serviceFieldName, pk, voJavaType.getShortName()));

		builder.withMethod(method);
	}

}
