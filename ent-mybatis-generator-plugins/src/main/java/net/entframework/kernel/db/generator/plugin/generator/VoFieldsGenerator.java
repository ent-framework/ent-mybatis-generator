/*
 * ******************************************************************************
 *  * Copyright (c) 2022-2023. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */

package net.entframework.kernel.db.generator.plugin.generator;

import net.entframework.kernel.db.generator.Constants;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.Context;

import java.util.List;

public class VoFieldsGenerator {

    private final Context context;

    private final String codingStyle;

    private final String voTargetPackage;

    private String voSuffix = "";

    private final FullyQualifiedJavaType factory;

    public VoFieldsGenerator(Context context, String codingStyle, String voTargetPackage,
                             String voSuffix,
                             FullyQualifiedJavaType factory) {
        this.context = context;
        this.codingStyle = codingStyle;
        this.voTargetPackage = voTargetPackage;
        this.voSuffix = voSuffix;
        this.factory = factory;
    }

    public FieldAndImports generateVo(TopLevelClass modelClass, IntrospectedTable introspectedTable) {
        String modelObjectName = modelClass.getType().getShortNameWithoutTypeArguments();
        FieldAndImports.Builder builder = new FieldAndImports.Builder();

        List<Field> fields = modelClass.getFields();
        for (Field field : fields) {
            if ("serialVersionUID".equals(field.getName()) || GeneratorUtils.isLogicDeleteField(field)) {
                continue;
            }
            Field pojoRequestField = new Field(field);
            // 清除源字段的Annotation
            pojoRequestField.getAnnotations().clear();
            FullyQualifiedJavaType fieldType = field.getType();
            if (GeneratorUtils.isRelationField(introspectedTable, field)) {
                fieldType = getActualJavaType(field);
            }
            InnerEnum innerEnum = (InnerEnum) field.getAttribute(Constants.TABLE_ENUM_FIELD_ATTR);
            if (innerEnum != null) {
                FullyQualifiedJavaType modelJavaType = GeneratorUtils.getModelJavaType(context, modelObjectName,
                        factory);
                fieldType = factory.create(modelJavaType.getFullyQualifiedName() + "." + field.getType());
                builder.withImport(fieldType);
            }
            pojoRequestField.setType(fieldType);
            pojoRequestField.setVisibility(JavaVisibility.PRIVATE);
            if (field.getAttribute(Constants.FIELD_LOGIC_DELETE_ATTR) != null) {
                pojoRequestField.setAttribute(Constants.FIELD_LOGIC_DELETE_ATTR,
                        field.getAttribute(Constants.FIELD_LOGIC_DELETE_ATTR));
            }
            String fieldDescription = field.getDescription();
            // 如果是关联关系
            if (GeneratorUtils.isRelationField(introspectedTable, field)) {
                builder.withImport(fieldType);
                pojoRequestField.setAttribute(Constants.FIELD_RELATION, field.getAttribute(Constants.FIELD_RELATION));
            }
            else {
                builder.withImport(fieldType);
                IntrospectedColumn column = GeneratorUtils.getIntrospectedColumnByJavaProperty(introspectedTable,
                        field.getName());

                // 增加通用的注解
                if (GeneratorUtils.isPrimaryKey(introspectedTable, column)) {
                    if (column.isNumberColumn()) {
                        pojoRequestField.addAnnotation(String.format(
                                "@NotNull(message = \"%s不能为空\", groups = {update.class, delete.class, detail.class, updateStatus.class})",
                                fieldDescription));
                        builder.withImport("jakarta.validation.constraints.NotNull");
                    }
                    else if (column.isStringColumn()) {
                        pojoRequestField.addAnnotation(String.format(
                                "@NotBlank(message = \"%s不能为空\", groups = {update.class, delete.class, detail.class, updateStatus.class})",
                                fieldDescription));
                        builder.withImport("jakarta.validation.constraints.NotBlank");
                    }
                }
                else if (!column.isNullable()) {
                    if (column.isStringColumn()) {
                        pojoRequestField.addAnnotation(
                                String.format("@NotBlank(message = \"%s不能为空\", groups = {add.class, update.class})",
                                        fieldDescription));
                        builder.withImport("jakarta.validation.constraints.NotBlank");
                    }
                }
            }
            if (codingStyle.equals(Constants.GENERATED_CODE_STYLE)) {
                pojoRequestField.addAnnotation(String.format("@Description(\"%s\")", fieldDescription));
                builder.withImport("io.entframework.kernel.core.annotation.Description");
            }

            builder.withField(pojoRequestField);
        }

        return builder.build();
    }

    // 获取字段的实际类型
    private FullyQualifiedJavaType getActualJavaType(Field field) {
        String fieldTypeName = field.getType().getFullyQualifiedNameWithoutTypeParameters();
        if ("java.util.List".equals(fieldTypeName)) {
            FullyQualifiedJavaType fullyQualifiedJavaType = FullyQualifiedJavaType.getNewListInstance();
            List<FullyQualifiedJavaType> typeArgs = field.getType().getTypeArguments();
            if (typeArgs != null) {
                for (FullyQualifiedJavaType type : typeArgs) {
                    fullyQualifiedJavaType.addTypeArgument(getVoJavaType(type.getShortName()));
                }
            }
            return fullyQualifiedJavaType;
        }
        else {
            return getVoJavaType(field.getType().getShortName());
        }
    }

    public FullyQualifiedJavaType getVoJavaType(String modelObjectName) {
        return factory.create(this.voTargetPackage + "." + modelObjectName + this.voSuffix);
    }

}
