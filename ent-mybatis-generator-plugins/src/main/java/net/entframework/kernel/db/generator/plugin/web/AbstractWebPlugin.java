/*
 * ******************************************************************************
 *  * Copyright (c) 2023. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */

package net.entframework.kernel.db.generator.plugin.web;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.plugin.AbstractDynamicSQLPlugin;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.WriteMode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbstractWebPlugin extends AbstractDynamicSQLPlugin {

    public static final String API_ROOT = "api";
    public static final String MODEL_ROOT = "model";
    public static final String ROUTER_ROOT = "routes";
    public static final String VIEW_ROOT = "views";

    /** global properties set in context -- start **/
    protected String projectRootAlias = "";
    protected String typescriptModelPackage;
    protected String apiPackage;
    protected String viewPackage;
    protected String enumPackage;
    protected String codingStyle;
    protected String outputSubFolder;

    /** global properties set in context -- end **/
    @Override
    public boolean validate(List<String> warnings) {

        this.typescriptModelPackage = this.context.getJavaModelGeneratorConfiguration().getTargetPackage();
        if (StringUtils.isAnyEmpty(this.typescriptModelPackage)) {
            warnings.add("请在javaModelGenerator节点中targetPackage属性");
            return false;
        }

        this.apiPackage = API_ROOT;
        this.enumPackage = MODEL_ROOT + ".enum";
        this.viewPackage = VIEW_ROOT;

        this.outputSubFolder = this.context.getProperty("outputSubFolder");
        if (StringUtils.isNotEmpty(this.outputSubFolder)) {
            this.apiPackage = API_ROOT + "." + this.outputSubFolder;
            this.enumPackage = MODEL_ROOT + "." + this.outputSubFolder + ".enum";
            this.viewPackage = VIEW_ROOT + "." + this.outputSubFolder;
        }


        codingStyle = this.context.getProperty("generatedCodeStyle");
        if (StringUtils.isEmpty(codingStyle)) {
            codingStyle = Constants.GENERATED_CODE_STYLE;
        }

        this.projectRootAlias = this.context.getProperty("projectRootAlias");
        String mode = this.properties.getProperty("writeMode");
        if (StringUtils.isNotEmpty(mode)) {
            WriteMode writeMode = convert(mode);
            if (writeMode != null) {
                this.writeMode = writeMode;
            }
            else {
                warnings.add(this.getClass().getName() + "配置了错误的WriteMode, 可用值: NEVER,OVER_WRITE,SKIP_ON_EXIST");
                return false;
            }
        }
        return true;
    }

    protected Set<String> getListIgnoreFields() {
        Set<String> fields = new HashSet<>();
        String listIgnoreFields = this.context.getProperty("listIgnoreFields");
        if (StringUtils.isNotBlank(listIgnoreFields)) {
            fields.addAll(List.of(StringUtils.split(listIgnoreFields, ",")));
        }

        listIgnoreFields = this.properties.getProperty("listIgnoreFields");
        if (StringUtils.isNotBlank(listIgnoreFields)) {
            fields.addAll(List.of(StringUtils.split(listIgnoreFields, ",")));
        }
        return fields;
    }

    protected Set<String> getInputIgnoreFields() {
        Set<String> fields = new HashSet<>();
        String listIgnoreFields = this.context.getProperty("inputIgnoreFields");
        if (StringUtils.isNotBlank(listIgnoreFields)) {
            fields.addAll(List.of(StringUtils.split(listIgnoreFields, ",")));
        }

        listIgnoreFields = this.properties.getProperty("inputIgnoreFields");
        if (StringUtils.isNotBlank(listIgnoreFields)) {
            fields.addAll(List.of(StringUtils.split(listIgnoreFields, ",")));
        }
        return fields;
    }


}
