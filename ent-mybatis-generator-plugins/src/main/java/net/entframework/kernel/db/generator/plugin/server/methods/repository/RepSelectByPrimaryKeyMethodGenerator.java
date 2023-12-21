/*
 * ******************************************************************************
 *  * Copyright (c) 2022. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */
package net.entframework.kernel.db.generator.plugin.server.methods.repository;

import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import net.entframework.kernel.db.generator.plugin.server.methods.AbstractMethodGenerator;
import net.entframework.kernel.db.generator.plugin.server.methods.MethodAndImports;
import net.entframework.kernel.db.generator.plugin.server.methods.Utils;
import net.entframework.kernel.db.generator.utils.CommentHelper;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

import java.util.*;

public class RepSelectByPrimaryKeyMethodGenerator extends AbstractMethodGenerator {

    public RepSelectByPrimaryKeyMethodGenerator(BuildConfig builder) {
        super(builder);
    }

    @Override
    public MethodAndImports generateMethodAndImports() {
        if (!Utils.generateSelectByPrimaryKey(introspectedTable)) {
            return null;
        }

        Set<FullyQualifiedJavaType> imports = new HashSet<>();

        imports.add(recordType);

        Method getByPrimaryKey = new Method("get");
        getByPrimaryKey.setAbstract(isAbstract);
        getByPrimaryKey.setReturnType(recordType);

        IntrospectedColumn pk = GeneratorUtils.getPrimaryKey(introspectedTable);
        Parameter parameter = new Parameter(new FullyQualifiedJavaType("Serializable"), pk.getJavaProperty());
        imports.add(new FullyQualifiedJavaType("java.io.Serializable"));
        getByPrimaryKey.addParameter(parameter);

        if (this.isAbstract) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("RepositoryName", getRepositoryJavaType().getShortName());
            variables.put("EntityName", recordType.getShortName());
            variables.put("MapperName", getMapperJavaType().getShortName());
            variables.put("ParamType", pk.getFullyQualifiedJavaType().getFullyQualifiedName());

            GeneratorUtils.addComment(getByPrimaryKey,
                    CommentHelper.INSTANCE.getComments("get", "Repository", variables));
        }
        else {
            GeneratorUtils.addComment(getByPrimaryKey, "{@inheritDoc}");
        }

        MethodAndImports.Builder builder = MethodAndImports.withMethod(getByPrimaryKey).withImports(imports);

        if (!isAbstract) {

            getByPrimaryKey.addAnnotation("@Override");
            getByPrimaryKey.setVisibility(JavaVisibility.PUBLIC);

            builder.withImport(new FullyQualifiedJavaType("java.util.Optional"));
            if (GeneratorUtils.hasRelation(context, introspectedTable)) {
                getByPrimaryKey.addBodyLine(String.format("%s %sQuery = new %s();", recordType.getShortName(),
                        StringUtils.uncapitalize(recordType.getShortName()), recordType.getShortName()));
                getByPrimaryKey.addBodyLine(String.format("ReflectionKit.setFieldValue(%sQuery, \"%s\", %s);",
                        StringUtils.uncapitalize(recordType.getShortName()), pk.getJavaProperty(),
                        pk.getJavaProperty()));
                getByPrimaryKey.addBodyLine(String.format("Optional<%s> row = this.selectOne(%sQuery);",
                        recordType.getShortName(), StringUtils.uncapitalize(recordType.getShortName())));
                builder.withImport("io.entframework.kernel.core.util.ReflectionKit");
                addLogicDeletedConvert(getByPrimaryKey, builder, pk);
            }
            else {
                getByPrimaryKey.addBodyLine(
                        String.format("Optional<%s> row = baseMapper.selectByPrimaryKey(getEntityClass(), %s);",
                                recordType.getShortName(), pk.getJavaProperty()));
                addLogicDeletedConvert(getByPrimaryKey, builder, pk);
            }

        }

        return builder.build();
    }

    public void addLogicDeletedConvert(Method getByPrimaryKey, MethodAndImports.Builder builder,
            IntrospectedColumn pk) {
        Optional<IntrospectedColumn> logicDelete = GeneratorUtils.getLogicDeleteColumn(introspectedTable);
        if (logicDelete.isPresent()) {
            IntrospectedColumn column = logicDelete.get();
            getByPrimaryKey.addBodyLine("if (row.isPresent()) {");
            getByPrimaryKey.addBodyLine(String.format("%s %s =  row.get();", recordType.getShortName(),
                    StringUtils.uncapitalize(recordType.getShortName())));
            getByPrimaryKey.addBodyLine(String.format("if (%s.get%s() != YesOrNotEnum.N) {",
                    StringUtils.uncapitalize(recordType.getShortName()),
                    StringUtils.capitalize(column.getJavaProperty())));
            getByPrimaryKey.addBodyLine(String.format("throw new DaoException(DaoExceptionEnum.GET_RECORD_ERROR, %s);",
                    pk.getJavaProperty()));
            getByPrimaryKey.addBodyLine("}");
            getByPrimaryKey
                    .addBodyLine(String.format("return %s;", StringUtils.uncapitalize(recordType.getShortName())));
            getByPrimaryKey.addBodyLine("}");
            getByPrimaryKey.addBodyLine(String.format("throw new DaoException(DaoExceptionEnum.GET_RECORD_ERROR, %s);",
                    pk.getJavaProperty()));
            builder.withImport(new FullyQualifiedJavaType("io.entframework.kernel.db.api.exception.DaoException"));
            builder.withImport(
                    new FullyQualifiedJavaType("io.entframework.kernel.db.api.exception.enums.DaoExceptionEnum"));
            builder.withImport(new FullyQualifiedJavaType("io.entframework.kernel.core.enums.YesOrNotEnum"));
        }
        else {

            getByPrimaryKey.addBodyLine(String.format(
                    "return row.orElseThrow(() -> new DaoException(DaoExceptionEnum.GET_RECORD_ERROR, %s));",
                    pk.getJavaProperty()));
            // getByPrimaryKey.addBodyLine("return row.<DaoException>orElseThrow(() ->
            // {");
            // getByPrimaryKey.addBodyLine(String.format("throw new
            // DaoException(DaoExceptionEnum.GET_RECORD_ERROR, %s);",
            // pk.getJavaProperty()));
            // getByPrimaryKey.addBodyLine("});");
        }
    }

}
