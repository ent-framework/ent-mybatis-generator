package net.entframework.kernel.db.generator.typescript.runtime;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

public class FullyQualifiedTypescriptType extends FullyQualifiedJavaType {

    private final String projectRootAlias;
    private final boolean isType;

    public FullyQualifiedTypescriptType(String fullTypeSpecification) {
        super(fullTypeSpecification);
        this.projectRootAlias = "";
        this.isType = false;
    }

    public FullyQualifiedTypescriptType(String fullTypeSpecification, boolean isType) {
        super(fullTypeSpecification);
        this.projectRootAlias = "";
        this.isType = isType;
    }

    public FullyQualifiedTypescriptType(String projectRootAlias, String fullTypeSpecification, boolean isType) {
        super(fullTypeSpecification);
        this.projectRootAlias = StringUtils.defaultString(projectRootAlias, "");
        this.isType = isType;
    }

    public String getProjectRootAlias() {
        return projectRootAlias;
    }

    public String getPackagePath() {
        return this.projectRootAlias + this.getPackageName().replaceAll("\\.", "/");
    }

    public String getFileName() {
        return StringUtils.substringAfterLast(this.getPackageName(), ".");
    }

    @Override
    public FullyQualifiedJavaType create(FullyQualifiedJavaType type) {
        return super.create(type);
    }

    @Override
    public FullyQualifiedJavaType create(String type) {
        return super.create(type);
    }

    public boolean isInterface() {
        return this.isType;
    }
}
