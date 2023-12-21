package net.entframework.kernel.db.generator.typescript.runtime;

import net.entframework.kernel.db.generator.typescript.render.TopLevelClassRenderer;
import net.entframework.kernel.db.generator.typescript.render.TopLevelEnumerationRenderer;
import net.entframework.kernel.db.generator.typescript.render.TopLevelInterfaceRenderer;
import org.mybatis.generator.api.JavaFormatter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.Context;

public class DefaultTypescriptFormatter implements JavaFormatter, CompilationUnitVisitor<String> {

    protected Context context;

    public DefaultTypescriptFormatter() {
    }

    public DefaultTypescriptFormatter(Context context) {
        this.context = context;
    }

    @Override
    public String getFormattedContent(CompilationUnit compilationUnit) {
        return compilationUnit.accept(this);
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public String visit(TopLevelClass topLevelClass) {
        return new TopLevelClassRenderer().render(topLevelClass);
    }

    @Override
    public String visit(TopLevelEnumeration topLevelEnumeration) {
        return new TopLevelEnumerationRenderer().render(topLevelEnumeration);
    }

    @Override
    public String visit(Interface topLevelInterface) {
        return new TopLevelInterfaceRenderer().render(topLevelInterface);
    }

}
