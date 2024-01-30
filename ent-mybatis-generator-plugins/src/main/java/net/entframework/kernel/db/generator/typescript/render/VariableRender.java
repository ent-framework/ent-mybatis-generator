package net.entframework.kernel.db.generator.typescript.render;

import net.entframework.kernel.db.generator.typescript.runtime.Variable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;

import java.util.ArrayList;
import java.util.List;

public class VariableRender {

	public List<String> render(Variable variable, CompilationUnit compilationUnit) {

		List<String> lines = new ArrayList<>(variable.getJavaDocLines());
		lines.add(renderVariable(variable, compilationUnit));

		return lines;
	}

	private String renderVariable(Variable variable, CompilationUnit compilationUnit) {
		StringBuilder sb = new StringBuilder();
		sb.append("const ");
		sb.append(variable.getName());
		sb.append(" = ");
		if (variable.getInitialization().getBodyLines().size() == 1) {
			sb.append(variable.getInitialization().getBodyLines().get(0));
		}
		sb.append(';');

		return sb.toString();
	}

}
