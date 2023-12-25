/*
 * ******************************************************************************
 *  * Copyright (c) 2022-2023. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */

package net.entframework.kernel.db.generator.plugin.server;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/***
 * Mapstruct 生成
 */
public class MapstructPlugin extends AbstractServerPlugin {

    @Override
    public boolean validate(List<String> warnings) {
        boolean validate = super.validate(warnings);

        if (StringUtils.isAnyEmpty(this.mapstructTargetPackage, this.mapstructSuffix)) {
            warnings.add("请检查MapstructPlugin配置");
            return false;
        }

        return validate;
    }

    /***
     * 在model产生后新增pojo request 和 pojo response 插件注册时要注意顺序，因为需要从TopLevelClass读取所有Field
     * @param topLevelClass the generated base record class
     * @param introspectedTable The class containing information about the table as
     * introspected from the database
     * @return
     */
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

        generatedJavaFiles.add(generateMapstruct(topLevelClass, introspectedTable));

        return true;
    }

    /***
     * 产生MapStrut接口文件
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    private GeneratedJavaFile generateMapstruct(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String modelObjectName = topLevelClass.getType().getShortNameWithoutTypeArguments();
        FullyQualifiedJavaType mapstructJavaType = getMapstructJavaType(modelObjectName);
        Interface mapstructInterface = new Interface(mapstructJavaType);
        mapstructInterface.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType pojoRequestJavaType = getVoJavaType(modelObjectName);
        FullyQualifiedJavaType recordType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        mapstructInterface.addImportedType(pojoRequestJavaType);
        mapstructInterface.addImportedType(topLevelClass.getType());

        Set<FullyQualifiedJavaType> relations = Utils.getRelatedFieldType(introspectedTable);
        InnerInterface entityToVoConverter = createInnerMapper(mapstructInterface, "EntityToVoConverter", recordType, pojoRequestJavaType);
        mapstructInterface.addImportedTypes(addDefaultRelation(relations, recordType,  entityToVoConverter, false));
        mapstructInterface.addInnerInterface(entityToVoConverter);

        InnerInterface voToEntityConverter = createInnerMapper(mapstructInterface, "VoToEntityConverter", pojoRequestJavaType, recordType);
        mapstructInterface.addImportedTypes(addDefaultRelation(relations, recordType, voToEntityConverter, true));
        mapstructInterface.addInnerInterface(voToEntityConverter);

        GeneratedJavaFile generatedJavaFile = new GeneratedJavaFile(mapstructInterface,
                context.getJavaModelGeneratorConfiguration().getTargetProject(),
                context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
        generatedJavaFile.setOutputDirectory(getOutputDirectory());
        return generatedJavaFile;
    }

    private Set<FullyQualifiedJavaType> addDefaultRelation(Set<FullyQualifiedJavaType> relations, FullyQualifiedJavaType recordType,
            InnerInterface innerInterface, boolean isRequest) {
        if (relations.isEmpty())
            return new HashSet<>();
        Set<FullyQualifiedJavaType> importTypes = new HashSet<>();
        relations.forEach(relationType -> {
            FullyQualifiedJavaType voJavaType = getVoJavaType(relationType.getShortName());
            if (!relationType.equals(recordType)) {
                if (isRequest) {
                    importTypes.addAll(createDefaultConverterMethod(innerInterface,
                            voJavaType, relationType));
                }
                else {
                    importTypes.addAll(createDefaultConverterMethod(innerInterface, relationType,
                            voJavaType));
                }
            }

        });
        return importTypes;
    }

    private Set<FullyQualifiedJavaType> createDefaultConverterMethod(InnerInterface innerInterface,
            FullyQualifiedJavaType sourceType, FullyQualifiedJavaType targetType) {
        Set<FullyQualifiedJavaType> importTypes = new HashSet<>();
        importTypes.add(sourceType);
        importTypes.add(targetType);
        Method method = new Method(String.format("map%sTo%s", sourceType.getShortName(), targetType.getShortName()));
        method.setDefault(true);
        method.setReturnType(targetType);
        method.addParameter(new Parameter(sourceType, StringUtils.uncapitalize(sourceType.getShortName())));
        method.addBodyLine(
                "ObjectConversionService converterService = SpringUtil.getBean(ObjectConversionService.class);");
        method.addBodyLine(String.format("return converterService.convert(%s, %s.class);",
                StringUtils.uncapitalize(sourceType.getShortName()), targetType.getShortName()));
        importTypes.add(new FullyQualifiedJavaType("net.entframework.kernel.converter.support.ObjectConversionService"));
        importTypes.add(new FullyQualifiedJavaType("cn.hutool.extra.spring.SpringUtil"));
        innerInterface.addMethod(method);
        return importTypes;
    }

    private InnerInterface createInnerMapper(Interface mapstructInterface, String interfaceName,
            FullyQualifiedJavaType sourceType, FullyQualifiedJavaType targetType) {
        InnerInterface innerInterface = new InnerInterface(new FullyQualifiedJavaType(interfaceName));
        mapstructInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.Mapper"));
        mapstructInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.NullValueCheckStrategy"));
        mapstructInterface.addImportedType(new FullyQualifiedJavaType("org.mapstruct.NullValuePropertyMappingStrategy"));
        innerInterface.addAnnotation(
                "@Mapper(componentModel = \"spring\", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)");
        innerInterface.addSuperInterface(generatorConverter(mapstructInterface, sourceType, targetType));
        return innerInterface;
    }

    private FullyQualifiedJavaType generatorConverter(Interface mapstructInterface, FullyQualifiedJavaType sourceType,
            FullyQualifiedJavaType targetType) {

        FullyQualifiedJavaType converter = new FullyQualifiedJavaType(
                "net.entframework.kernel.converter.support.ObjectConverter");
        mapstructInterface.addImportedType(converter);
        converter.addTypeArgument(sourceType);
        converter.addTypeArgument(targetType);
        return converter;

    }

    /**
     * @return
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
        return generatedJavaFiles;
    }

}
