/*
 * ******************************************************************************
 *  * Copyright (c) 2022-2023. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */

package net.entframework.kernel.db.generator.plugin.server;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import net.entframework.kernel.db.generator.plugin.generator.RestMethod;
import net.entframework.kernel.db.generator.plugin.generator.RestMethodAndImports;
import net.entframework.kernel.db.generator.plugin.generator.ServerRestMethodsGenerator;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.WriteMode;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

/***
 * Controller 生成
 */
public class ControllerPlugin extends AbstractServerPlugin {

    private String responseBodyWrapper = "io.entframework.kernel.core.vo.ResponseData";

    private String responseBodySuccessStaticMethod = "ok";

    private boolean enableControllerParentMode = false;

    @Override
    public boolean validate(List<String> warnings) {
        boolean validate = super.validate(warnings);

        if (StringUtils.isAnyEmpty(this.controllerTargetPackage)) {
            warnings.add("请检查ControllerPlugin配置");
            return false;
        }

        String enableControllerParentMode = this.properties.getProperty("enableControllerParentMode");
        if (StringUtility.stringHasValue(enableControllerParentMode)
                && StringUtility.isTrue(enableControllerParentMode)) {
            this.enableControllerParentMode = true;
        }

        return validate;
    }

    /**
     * Mapper 文件生成时，同步Controller
     * @param interfaze the generated interface if any, may be null
     * @param introspectedTable The class containing information about the table as
     * introspected from the database
     * @return
     */
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {

        String relTable = introspectedTable.getTableConfiguration().getProperty(pluginName);
        if (relTable != null && relTable.equalsIgnoreCase("false")) {
            return true;
        }

        //检查是否中间表
        if (super.isManyToManyMiddleTable(introspectedTable)) {
            return true;
        }

        String baseRecordType = introspectedTable.getBaseRecordType();
        FullyQualifiedJavaType recordType = new FullyQualifiedJavaType(baseRecordType);
        FullyQualifiedJavaType baseControllerJavaType = getControllerJavaType(recordType.getShortName());
        // 启用抽象父类
        if (this.enableControllerParentMode) {
            baseControllerJavaType = getBaseControllerJavaType(recordType.getShortName());
        }
        TopLevelClass baseControllerJavaClass = new TopLevelClass(baseControllerJavaType);
        baseControllerJavaClass.setVisibility(JavaVisibility.PUBLIC);
        baseControllerJavaClass.setAbstract(this.enableControllerParentMode);
        // 启用抽象父类，父类强制覆盖
        if (this.enableControllerParentMode) {
            baseControllerJavaClass.setWriteMode(WriteMode.OVER_WRITE);
        }

        FullyQualifiedJavaType converterServiceJavaType = new FullyQualifiedJavaType("io.entframework.kernel.converter.support.ConverterService");
        baseControllerJavaClass.addImportedType(converterServiceJavaType);
        String csFieldName = super.lowerCaseFirstChar(converterServiceJavaType.getShortName());
        Field csField = new Field(csFieldName, converterServiceJavaType);

        csField.setVisibility(JavaVisibility.PROTECTED);
        csField.addAnnotation("@Autowired");
        csField.addAnnotation("@Lazy");
        baseControllerJavaClass.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        baseControllerJavaClass.addImportedType("org.springframework.context.annotation.Lazy");
        baseControllerJavaClass.addField(csField);


        FullyQualifiedJavaType serviceJavaType = getServiceJavaType(recordType.getShortName());
        baseControllerJavaClass.addImportedType(serviceJavaType);
        String serviceFieldName = super.lowerCaseFirstChar(serviceJavaType.getShortName());
        Field serviceField = new Field(serviceFieldName, serviceJavaType);

        serviceField.setVisibility(JavaVisibility.PROTECTED);
        serviceField.addAnnotation("@Resource");
        baseControllerJavaClass.addImportedType("jakarta.annotation.Resource");
        baseControllerJavaClass.addField(serviceField);

        String modelDescription = interfaze.getDescription();
        TopLevelClass modelClass = (TopLevelClass) introspectedTable
                .getAttribute(Constants.INTROSPECTED_TABLE_MODEL_CLASS);

        FullyQualifiedJavaType voJavaType = getVoJavaType(recordType.getShortName());
        IntrospectedColumn pkColumn = GeneratorUtils.getPrimaryKey(introspectedTable);
        ServerRestMethodsGenerator restMethodsGenerator = new ServerRestMethodsGenerator(recordType, voJavaType,
                serviceFieldName, pkColumn, true);
        if (StringUtils.isNotEmpty(this.voRootClass)) {
            restMethodsGenerator.setBaseVoType(new FullyQualifiedJavaType(this.voRootClass));
        }
        restMethodsGenerator.generate();
        RestMethodAndImports methodAndImports = restMethodsGenerator.build();

        methodAndImports.getMethods().forEach(method -> {
            if (StringUtils.equals("POST", method.getHttpMethod())) {
                addPostMapping(baseControllerJavaClass, method, modelDescription);
            }
            if (StringUtils.equals("GET", method.getHttpMethod())) {
                addGetMapping(baseControllerJavaClass, method, modelDescription);
            }
            if (StringUtils.isAnyEmpty(this.responseBodyWrapper, this.responseBodySuccessStaticMethod)) {
                //method.addBodyLine(String.format("return %s.%s(request);", serviceFieldName, method.getName()));
                method.addBodyLine("return result;");
            }
            else {
                FullyQualifiedJavaType responseWrapJavaType = new FullyQualifiedJavaType(this.responseBodyWrapper);
                FullyQualifiedJavaType responseBodyWrapperType = new FullyQualifiedJavaType(this.responseBodyWrapper);
                baseControllerJavaClass.addImportedType(responseWrapJavaType);
                if (method.getReturnType().isPresent()) {
                    responseBodyWrapperType.addTypeArgument(method.getReturnType().get());
                    method.setReturnType(responseBodyWrapperType);
                }
                method.addBodyLine(String.format("return %s.%s(result);", responseWrapJavaType.getShortName(),
                        this.responseBodySuccessStaticMethod));
            }
        });

        methodAndImports.getMethods().forEach(baseControllerJavaClass::addMethod);
        baseControllerJavaClass.addImportedTypes(methodAndImports.getImports());

        GeneratedJavaFile baseControllerJavaFile = new GeneratedJavaFile(baseControllerJavaClass,
                context.getJavaModelGeneratorConfiguration().getTargetProject(),
                context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());

        baseControllerJavaFile.setOutputDirectory(getOutputDirectory());

        if (this.enableControllerParentMode) {
            FullyQualifiedJavaType controllerJavaType = getControllerJavaType(recordType.getShortName());
            TopLevelClass controllerJavaClass = new TopLevelClass(controllerJavaType);
            controllerJavaClass.setVisibility(JavaVisibility.PUBLIC);
            controllerJavaClass.addAnnotation("@RestController");
            controllerJavaClass.addImportedType(
                    new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RestController"));
            controllerJavaClass.setSuperClass(baseControllerJavaType);
            controllerJavaClass.addImportedType(baseControllerJavaType);

            if (this.codingStyle.equals(Constants.GENERATED_CODE_STYLE)) {
                controllerJavaClass
                        .addAnnotation(String.format("@ApiResource(displayName = \"%s\")", modelClass.getDescription()));
                controllerJavaClass.addImportedType("io.entframework.kernel.scanner.api.annotation.ApiResource");
            }

            // 子类默认只新增，不覆盖
            controllerJavaClass.setWriteMode(this.writeMode == null ? WriteMode.SKIP_ON_EXIST : this.writeMode);

            GeneratedJavaFile controllerJavaFile = new GeneratedJavaFile(controllerJavaClass,
                    context.getJavaModelGeneratorConfiguration().getTargetProject(),
                    context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
            controllerJavaFile.setOutputDirectory(getOutputDirectory());
            this.generatedJavaFiles.add(controllerJavaFile);
        }
        else {
            if (this.codingStyle.equals(Constants.GENERATED_CODE_STYLE)) {
                baseControllerJavaClass
                        .addAnnotation(String.format("@ApiResource(displayName = \"%s\")", modelClass.getDescription()));
                baseControllerJavaClass.addImportedType("io.entframework.kernel.scanner.api.annotation.ApiResource");
            }
        }

        this.generatedJavaFiles.add(baseControllerJavaFile);

        return true;
    }

    private void addPostMapping(TopLevelClass controllerJavaClass, RestMethod method, String modelDescription) {
        if (this.codingStyle.equals(Constants.GENERATED_CODE_STYLE)) {
            method.addAnnotation(String.format("@PostResource(displayName = \"%s-%s\", path = \"%s\")", modelDescription,
                    method.getOperation(), method.getRestPath()));
            controllerJavaClass.addImportedType("io.entframework.kernel.scanner.api.annotation.PostResource");
        }
        else {
            method.addAnnotation(String.format("@PostMapping(\"%s\")", method.getRestPath()));
            controllerJavaClass.addImportedType("org.springframework.web.bind.annotation.PostMapping");
        }
    }

    private void addGetMapping(TopLevelClass controllerJavaClass, RestMethod method, String modelDescription) {
        if (this.codingStyle.equals(Constants.GENERATED_CODE_STYLE)) {
            method.addAnnotation(String.format("@GetResource(displayName = \"%s-%s\", path = \"%s\")", modelDescription,
                    method.getOperation(), method.getRestPath()));
            controllerJavaClass.addImportedType("io.entframework.kernel.scanner.api.annotation.GetResource");
        }
        else {
            method.addAnnotation(String.format("@GetMapping(\"/%s\")", method.getRestPath()));
            controllerJavaClass.addImportedType("org.springframework.web.bind.annotation.GetMapping");
        }
    }

    /**
     * @return
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
        return generatedJavaFiles;
    }

}
