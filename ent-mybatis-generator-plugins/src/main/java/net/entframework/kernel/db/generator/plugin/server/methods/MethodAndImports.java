/*
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package net.entframework.kernel.db.generator.plugin.server.methods;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodAndImports {

	private final List<Method> methods;

	private final Set<FullyQualifiedJavaType> imports;

	private final Set<String> staticImports;

	private MethodAndImports(Builder builder) {
		methods = builder.methods;
		imports = builder.imports;
		staticImports = builder.staticImports;
	}

	public List<Method> getMethods() {
		return methods;
	}

	public Set<FullyQualifiedJavaType> getImports() {
		return imports;
	}

	public Set<String> getStaticImports() {
		return staticImports;
	}

	public static Builder withMethod(Method method) {
		return new Builder().withMethod(method);
	}

	public static class Builder {

		private final List<Method> methods = new ArrayList<>();

		private final Set<FullyQualifiedJavaType> imports = new HashSet<>();

		private final Set<String> staticImports = new HashSet<>();

		public Builder withMethod(Method method) {
			this.methods.add(method);
			return this;
		}

		public Builder withImport(FullyQualifiedJavaType importedType) {
			this.imports.add(importedType);
			return this;
		}

		public Builder withImport(String importedType) {
			this.imports.add(new FullyQualifiedJavaType(importedType));
			return this;
		}

		public Builder withImports(Set<FullyQualifiedJavaType> imports) {
			this.imports.addAll(imports);
			return this;
		}

		public Builder withStaticImport(String staticImport) {
			this.staticImports.add(staticImport);
			return this;
		}

		public Builder withStaticImports(Set<String> staticImports) {
			this.staticImports.addAll(staticImports);
			return this;
		}

		public MethodAndImports build() {
			return new MethodAndImports(this);
		}

	}

}
