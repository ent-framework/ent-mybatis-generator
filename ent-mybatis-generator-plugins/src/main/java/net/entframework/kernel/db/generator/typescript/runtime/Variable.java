package net.entframework.kernel.db.generator.typescript.runtime;

import org.mybatis.generator.api.dom.java.InitializationBlock;
import org.mybatis.generator.api.dom.java.JavaElement;

public class Variable extends JavaElement {

	private String name;

	private InitializationBlock initialization;

	public Variable(String name) {
		this.name = name;
		this.initialization = new InitializationBlock();
	}

	public String getName() {
		return name;
	}

	public InitializationBlock getInitialization() {
		return initialization;
	}

}
