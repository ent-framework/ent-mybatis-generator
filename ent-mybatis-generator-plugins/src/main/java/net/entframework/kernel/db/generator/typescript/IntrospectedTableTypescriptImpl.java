package net.entframework.kernel.db.generator.typescript;

import net.entframework.kernel.db.generator.typescript.runtime.GeneratedTypescriptFile;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntrospectedTableTypescriptImpl extends IntrospectedTable {

    protected final List<AbstractJavaGenerator> javaGenerators = new ArrayList<>();

    public IntrospectedTableTypescriptImpl() {
        super(TargetRuntime.MYBATIS3_DSQL);
    }

    @Override
    public void calculateGenerators(List<String> warnings, ProgressCallback progressCallback) {
        AbstractJavaGenerator javaGenerator = new TypescriptModelGenerator(getModelProject());
        initializeAbstractGenerator(javaGenerator, warnings, progressCallback);

        if (context.getJavaClientGeneratorConfiguration() != null) {
            TypescriptClientGenerator typescriptApiGenerator = new TypescriptClientGenerator(getModelProject());
            initializeAbstractGenerator(typescriptApiGenerator, warnings, progressCallback);
            javaGenerators.add(typescriptApiGenerator);
        }

        javaGenerators.add(javaGenerator);
    }

    private String getModelProject() {
        return context.getJavaModelGeneratorConfiguration().getTargetProject();
    }

    protected void initializeAbstractGenerator(AbstractGenerator abstractGenerator, List<String> warnings,
                                               ProgressCallback progressCallback) {
        if (abstractGenerator == null) {
            return;
        }
        abstractGenerator.setContext(context);
        abstractGenerator.setIntrospectedTable(this);
        abstractGenerator.setProgressCallback(progressCallback);
        abstractGenerator.setWarnings(warnings);
    }

    @Override
    public List<GeneratedJavaFile> getGeneratedJavaFiles() {
        List<GeneratedJavaFile> answer = new ArrayList<>();

        for (AbstractJavaGenerator javaGenerator : javaGenerators) {
            List<CompilationUnit> compilationUnits = javaGenerator.getCompilationUnits();
            for (CompilationUnit compilationUnit : compilationUnits) {
                GeneratedJavaFile gjf = new GeneratedTypescriptFile(compilationUnit, javaGenerator.getProject(),
                        context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
                answer.add(gjf);
            }
        }

        return answer;
    }

    @Override
    public List<GeneratedXmlFile> getGeneratedXmlFiles() {
        return Collections.emptyList();
    }

    @Override
    public List<GeneratedKotlinFile> getGeneratedKotlinFiles() {
        return Collections.emptyList();
    }

    @Override
    public int getGenerationSteps() {
        return 0;
    }

    @Override
    public boolean requiresXMLGenerator() {
        return false;
    }
}
