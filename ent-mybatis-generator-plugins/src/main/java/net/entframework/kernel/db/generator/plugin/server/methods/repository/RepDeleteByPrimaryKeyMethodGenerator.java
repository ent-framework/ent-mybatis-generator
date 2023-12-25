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

public class RepDeleteByPrimaryKeyMethodGenerator extends AbstractMethodGenerator {

    public RepDeleteByPrimaryKeyMethodGenerator(BuildConfig builder) {
        super(builder);
    }

    @Override
    public MethodAndImports generateMethodAndImports() {
        if (!Utils.generateDeleteByPrimaryKey(introspectedTable)) {
            return null;
        }

        Set<FullyQualifiedJavaType> imports = new HashSet<>();
        Method method = new Method("delete"); //$NON-NLS-1$
        method.setAbstract(isAbstract);
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, imports);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addParameter(new Parameter(recordType, "row"));

        if (this.isAbstract) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("RepositoryName", getRepositoryJavaType().getShortName());
            variables.put("EntityName", recordType.getShortName());
            GeneratorUtils.addComment(method,
                    CommentHelper.INSTANCE.getComments("deleteById", "Repository", variables));
        }
        else {
            GeneratorUtils.addComment(method, "{@inheritDoc}");
        }

        MethodAndImports.Builder builder = MethodAndImports.withMethod(method).withImports(imports);

        if (!isAbstract) {
            method.addAnnotation("@Override");
            method.setVisibility(JavaVisibility.PUBLIC);
            FullyQualifiedJavaType mapperSupportJavaType = getMapperSupportJavaType();

            IntrospectedColumn pk = GeneratorUtils.getPrimaryKey(introspectedTable);
            Optional<IntrospectedColumn> logicDelete = GeneratorUtils.getLogicDeleteColumn(introspectedTable);
            method.addBodyLine(String.format("if (row == null || row.get%s() == null) {",
                    StringUtils.capitalize(pk.getJavaProperty())));
            method.addBodyLine("throw new ServiceException(DefaultBusinessExceptionEnum.WRONG_ARGS_ERROR);");
            method.addBodyLine("}");
            builder.withImport(" net.entframework.kernel.core.exception.base.ServiceException");
            builder.withImport(" net.entframework.kernel.core.exception.enums.defaults.DefaultBusinessExceptionEnum");
            if (logicDelete.isPresent()) {
                IntrospectedColumn logicDeleteColumn = logicDelete.get();
                method.addBodyLine(String.format("row.set%s(YesOrNotEnum.Y);",
                        StringUtils.capitalize(logicDeleteColumn.getJavaProperty())));
                method.addBodyLine("return baseMapper.updateByPrimaryKey(row, false);");
            }
            else {
                Optional<IntrospectedColumn> version = GeneratorUtils.getVersionColumn(introspectedTable);
                if (version.isPresent()) {
                    builder.withImport(mapperSupportJavaType);
                    builder.withImport("org.mybatis.dynamic.sql.delete.DeleteDSL");
                    builder.withImport("org.mybatis.dynamic.sql.delete.DeleteModel");
                    builder.withImport("java.util.Objects");
                    builder.withStaticImport("org.mybatis.dynamic.sql.SqlBuilder.isEqualTo");
                    method.addBodyLine(
                            String.format("return baseMapper.delete(%s.class, c -> {", recordType.getShortName()));
                    method.addBodyLine("DeleteDSL<DeleteModel>.DeleteWhereBuilder deleteDSL = c.where();");
                    method.addBodyLine(
                            String.format("deleteDSL.and(%s.%s, isEqualTo(row.get%s()).filter(Objects::nonNull));",
                                    mapperSupportJavaType.getShortName(), pk.getJavaProperty(),
                                    StringUtils.capitalize(pk.getJavaProperty())));
                    IntrospectedColumn versionColumn = version.get();
                    method.addBodyLine(
                            String.format("deleteDSL.and(%s.%s, isEqualTo(row.get%s()).filter(Objects::nonNull));",
                                    mapperSupportJavaType.getShortName(), versionColumn.getJavaProperty(),
                                    StringUtils.capitalize(versionColumn.getJavaProperty())));
                    method.addBodyLine("return deleteDSL;");
                    method.addBodyLine("}");
                    method.addBodyLine(");");
                }
                else {
                    method.addBodyLine(String.format("return super.deleteByPrimaryKey(row.get%s());",
                            StringUtils.capitalize(pk.getJavaProperty())));
                }

            }

        }

        return builder.build();
    }

}
