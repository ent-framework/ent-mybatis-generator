package net.entframework.kernel.db.generator.typescript.runtime;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.ArrayList;
import java.util.List;

public class TypescriptTopLevelClass extends TopLevelClass {

	public TypescriptTopLevelClass(FullyQualifiedTypescriptType type) {
		super(type);
	}

	public TypescriptTopLevelClass(String typeName) {
		super(typeName);
	}

	@Override
	public void addImportedType(FullyQualifiedJavaType importedType) {
		if (importedType != null && importedType.isExplicitlyImported()
				&& !importedType.getShortName().equals(getType().getShortName())) {
			importedTypes.add(importedType);
		}
	}

	private List<Variable> variables = new ArrayList<>();

	public List<Variable> getVariables() {
		return variables;
	}

	public void addVariable(Variable var) {
		this.variables.add(var);
	}

}
