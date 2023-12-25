/*
 * ******************************************************************************
 *  * Copyright (c) 2022. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */
package net.entframework.kernel.db.generator.typescript;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import net.entframework.kernel.db.generator.plugin.generator.RestMethodAndImports;
import net.entframework.kernel.db.generator.plugin.generator.WebRestMethodsGenerator;
import net.entframework.kernel.db.generator.typescript.render.RenderingUtilities;
import net.entframework.kernel.db.generator.typescript.runtime.FullyQualifiedTypescriptType;
import net.entframework.kernel.db.generator.typescript.runtime.TypescriptTopLevelClass;
import net.entframework.kernel.db.generator.utils.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class TypescriptClientGenerator extends AbstractJavaClientGenerator {

    protected String projectRootAlias = "";
    protected String apiDefaultPrefix = "";
    protected String apiEnvName = "";

    public TypescriptClientGenerator(String project) {
        super(project, false);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        progressCallback.startTask(getString("Progress.17", //$NON-NLS-1$
                introspectedTable.getFullyQualifiedTable().toString()));
        preCalculate();

        Interface interfaze = createBasicInterface();

        List<CompilationUnit> answer = new ArrayList<>();

//        if (context.getPlugins().clientGenerated(interfaze, introspectedTable)) {
//            answer.add(interfaze);
//        }

        //answer.add(interfaze);
        answer.add(generateRestApiClass());
        return answer;
    }

    protected void preCalculate() {
        projectRootAlias = this.context.getProperty("projectRootAlias");
        if (StringUtils.isBlank(projectRootAlias)) {
            projectRootAlias = "";
        }
        apiDefaultPrefix = this.context.getProperty("apiDefaultPrefix");
        if (StringUtils.isBlank(apiDefaultPrefix)) {
            apiDefaultPrefix = "";
        }
        apiEnvName = this.context.getProperty("apiEnvName");
        if (StringUtils.isBlank(apiEnvName)) {
            apiEnvName = "";
        }
    }

    protected Interface createBasicInterface() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        String camelCaseName = JavaBeansUtil.convertCamelCase(table.getDomainObjectName(), "-");
        String typescriptModelPackage = this.context.getJavaClientGeneratorConfiguration().getTargetPackage();
        FullyQualifiedTypescriptType type =  new FullyQualifiedTypescriptType("", typescriptModelPackage + "." + camelCaseName+"." + table.getDomainObjectName(), true);
        Interface interfaze = new Interface(type);
        interfaze.setVisibility(JavaVisibility.PUBLIC);
        context.getCommentGenerator().addJavaFileComment(interfaze);
        return interfaze;
    }

    private TypescriptTopLevelClass generateRestApiClass() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        String modelObjectName = table.getDomainObjectName();
        String camelCaseName = JavaBeansUtil.convertCamelCase(modelObjectName, "-");
        FullyQualifiedTypescriptType tsApiModelJavaType = new FullyQualifiedTypescriptType(this.projectRootAlias,
                this.context.getJavaClientGeneratorConfiguration().getTargetPackage() + "." + camelCaseName + "." + modelObjectName, true);
        TypescriptTopLevelClass tsBaseModelClass = new TypescriptTopLevelClass(tsApiModelJavaType);

        tsBaseModelClass.setWriteMode(WriteMode.OVER_WRITE);

        tsBaseModelClass.setVisibility(JavaVisibility.PUBLIC);

        //GeneratorUtils.addComment(tsBaseModelClass, topLevelClass.getDescription() + " 服务请求类");

        String typescriptModelPackage = this.context.getJavaModelGeneratorConfiguration().getTargetPackage();

        FullyQualifiedJavaType recordType = new FullyQualifiedJavaType(typescriptModelPackage + "." + modelObjectName);

        FullyQualifiedJavaType requestJavaType = new FullyQualifiedTypescriptType(this.projectRootAlias,
                typescriptModelPackage + "." + WebUtils.getFileName(modelObjectName) + "." + modelObjectName, true);

        IntrospectedColumn pkColumn = GeneratorUtils.getPrimaryKey(introspectedTable);
        WebRestMethodsGenerator restMethodsGenerator = new WebRestMethodsGenerator(recordType, requestJavaType,
                "", pkColumn,false);
        restMethodsGenerator.generate();
        RestMethodAndImports methodAndImports = restMethodsGenerator.build();

        methodAndImports.getMethods().forEach(method -> {
            String methodName = method.getName();
            String returnTypeName = "void";
            if (method.getReturnType().isPresent()) {
                FullyQualifiedJavaType returnType = method.getReturnType().get();
                if (returnType.getFullyQualifiedNameWithoutTypeParameters()
                        .equals("net.entframework.kernel.db.api.pojo.page.PageResult")) {
                    FullyQualifiedJavaType arg = returnType.getTypeArguments().get(0);
                    String newTypeName = arg.getShortName() + "PageModel";
                    FullyQualifiedJavaType newType = new FullyQualifiedTypescriptType(this.projectRootAlias,
                            typescriptModelPackage + "." + JavaBeansUtil.convertCamelCase(arg.getShortName(), "-") + "."
                                    + newTypeName, true);
                    returnTypeName = newTypeName;
                    method.setReturnType(newType);
                    methodAndImports.getImports().removeIf(javaType -> javaType.equals(returnType));
                    methodAndImports.getImports().add(newType);
                }
                else {
                    returnTypeName = RenderingUtilities.calculateTypescriptTypeName(null, returnType);
                }
            }
            Parameter parameter = method.getParameters().get(0);
            if (StringUtils.equals("POST", method.getHttpMethod())) {
                method.addBodyLine(String.format("defHttp.post<%s>({ url: '%s%s', data: %s });", returnTypeName,
                        apiDefaultPrefix, method.getRestPath(), parameter.getName()));
            }
            if (StringUtils.equals("GET", method.getHttpMethod())) {
                method.addBodyLine(String.format("defHttp.get<%s>({ url: '%s%s', params: %s });", returnTypeName,
                        apiDefaultPrefix, method.getRestPath(), parameter.getName()));
            }
            method.setName(modelObjectName + StringUtils.capitalize(methodName));
        });

        methodAndImports.getMethods().forEach(tsBaseModelClass::addMethod);
        tsBaseModelClass.addImportedTypes(methodAndImports.getImports());
        tsBaseModelClass.setAttribute(Constants.WEB_PROJECT_ROOT_ALIAS, this.projectRootAlias);
        tsBaseModelClass
                .addImportedType(new FullyQualifiedTypescriptType("", "fe-ent-core.es.utils.http.defHttp", false));

        return tsBaseModelClass;
    }

    @Override
    public AbstractXmlGenerator getMatchedXMLGenerator() {
        return null;
    }
}
