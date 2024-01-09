/*
 * Copyright (c) 2024. Licensed under the Apache License, Version 2.0.
 */

package net.entframework.mybatis.apt;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import java.sql.JDBCType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class AnnotationMeta {

	private static final Map<String, Map<String, Object>> annotationData = new ConcurrentHashMap<>();

	private final VariableElement element;

	/**
	 * check <a href=
	 * "https://docs.oracle.com/javase/8/docs/api/javax/lang/model/element/AnnotationValue.html">docs
	 * </a>
	 * @param variableElement 变量
	 */
	public AnnotationMeta(VariableElement variableElement) {
		this.element = variableElement;
		List<? extends AnnotationMirror> elementAnnotationMirrors = variableElement.getAnnotationMirrors();
		for (AnnotationMirror annotationMirror : elementAnnotationMirrors) {
			System.out.println(((TypeElement) annotationMirror.getAnnotationType().asElement()).getQualifiedName());
			Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror
				.getElementValues();
			Map<String, Object> keyValueMap = new LinkedHashMap<>();
			elementValues.forEach((key, value) -> {
				Object gValue = value.getValue();
				if (gValue instanceof VariableElement) {
					JDBCType accepted = value.accept(new MyAnnotationValueVisitor(), null);
					if (accepted != null) {
						keyValueMap.put(key.getSimpleName().toString(), accepted);
					}
				}
				else {
					keyValueMap.put(key.getSimpleName().toString(), gValue);
				}
			});
			annotationData.put(
					((TypeElement) annotationMirror.getAnnotationType().asElement()).getQualifiedName().toString(),
					keyValueMap);
		}
	}

	public Map<String, Object> getAnnotationData(String annoName) {
		return annotationData.get(annoName);
	}

	public boolean hasAnnotation(String annoName) {
		return annotationData.containsKey(annoName);
	}

	public String fieldName() {
		return this.element.getSimpleName().toString();
	}

	public TypeMirror asType() {
		return this.element.asType();
	}

	public static class MyAnnotationValueVisitor extends SimpleAnnotationValueVisitor8<JDBCType, Void> {

		@Override
		public JDBCType visitEnumConstant(VariableElement c, Void unused) {
			return JDBCType.valueOf(c.getSimpleName().toString());
		}

	}

}
