package net.entframework.kernel.db.generator.typescript.render;

import net.entframework.kernel.db.generator.typescript.runtime.Variable;
import org.mybatis.generator.api.dom.java.CompilationUnit;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RenderUtils {

	private static final VariableRender variableRender = new VariableRender();

	public static List<String> renderVariables(List<Variable> variables, CompilationUnit compilationUnit) {
		return variables.stream().flatMap(f -> renderField(f, compilationUnit)).collect(Collectors.toList());
	}

	private static Stream<String> renderField(Variable variable, CompilationUnit compilationUnit) {
		return variableRender.render(variable, compilationUnit).stream();
	}

}
