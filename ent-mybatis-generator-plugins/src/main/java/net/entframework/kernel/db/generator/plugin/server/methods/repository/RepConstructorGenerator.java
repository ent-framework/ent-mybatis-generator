/*
 * ******************************************************************************
 *  * Copyright (c) 2022. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */
package net.entframework.kernel.db.generator.plugin.server.methods.repository;

import net.entframework.kernel.db.generator.plugin.server.methods.AbstractMethodGenerator;
import net.entframework.kernel.db.generator.plugin.server.methods.MethodAndImports;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.dom.java.*;

import java.util.HashSet;
import java.util.Set;

public class RepConstructorGenerator extends AbstractMethodGenerator {

	public RepConstructorGenerator(BuildConfig builder) {
		super(builder);
	}

	@Override
	public MethodAndImports generateMethodAndImports() {
		Set<FullyQualifiedJavaType> imports = new HashSet<>();
		Set<String> staticImports = new HashSet<>();

		imports.add(recordType);

		Method constructorMethod = new Method(this.hostJavaClass.getType().getShortName()); // $NON-NLS-1$
		constructorMethod.setConstructor(true);
		constructorMethod.setAbstract(isAbstract);

		FullyQualifiedJavaType parameterType = getMapperJavaType(); // $NON-NLS-1$
		imports.add(parameterType);
		constructorMethod
			.addParameter(new Parameter(parameterType, StringUtils.uncapitalize(parameterType.getShortName()))); // $NON-NLS-1$

		if (!isAbstract) {

			constructorMethod.setVisibility(JavaVisibility.PUBLIC);

			constructorMethod.addBodyLine(String.format("super(%s, %s.class);",
					StringUtils.uncapitalize(parameterType.getShortName()), recordType.getShortName()));

		}
		MethodAndImports.Builder builder = MethodAndImports.withMethod(constructorMethod)
			.withImports(imports)
			.withStaticImports(staticImports);

		return builder.build();
	}

}
