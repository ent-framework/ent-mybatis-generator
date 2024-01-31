/*
 * ******************************************************************************
 *  * Copyright (c) 2022. Licensed under the Apache License, Version 2.0.
 *  *****************************************************************************
 *
 */
package net.entframework.kernel.db.generator.plugin.server;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.JoinTarget;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

	public static Set<FullyQualifiedJavaType> getRelatedFieldType(IntrospectedTable introspectedTable) {
		Set<FullyQualifiedJavaType> relatedFieldType = new HashSet<>();
		TopLevelClass modelClass = introspectedTable.getBaseModelClass();
		if (GeneratorUtils.hasRelation(modelClass, JoinTarget.JoinType.MANY_TO_ONE)) {
			List<Field> fields = GeneratorUtils.getRelatedFields(modelClass, JoinTarget.JoinType.MANY_TO_ONE);
			if (fields.size() > 0) {
				for (Field field : fields) {
					relatedFieldType.add(field.getType());
				}
			}
		}

		if (GeneratorUtils.hasRelation(modelClass, JoinTarget.JoinType.ONE_TO_MANY)) {
			List<Field> fields = GeneratorUtils.getRelatedFields(modelClass, JoinTarget.JoinType.ONE_TO_MANY);
			if (fields.size() > 0) {
				for (Field field : fields) {
					relatedFieldType.add(field.getType().getTypeArguments().get(0));
				}
			}
		}
		return relatedFieldType;
	}

	public static String lowerCaseFirstChar(String s) {
		if (Character.isLowerCase(s.charAt(0))) {
			return s;
		}
		else {
			return Character.toLowerCase(s.charAt(0)) + s.substring(1);
		}
	}

}
