/*
 * Copyright (c) 2024. Licensed under the Apache License, Version 2.0.
 */

package net.entframework.mybatis.apt;

import com.squareup.javapoet.*;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Table;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor6;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.JDBCType;
import java.util.*;

@SupportedAnnotationTypes("jakarta.persistence.Entity")
public class MybatisProcessor extends AbstractProcessor {

	public static final Boolean ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS = Boolean.FALSE;

	public static final String PERSISTENCE_COLUMN_NAME = "jakarta.persistence.Column";

	public static final String SQL_COLUMN_NAME = "org.mybatis.dynamic.sql.annotation.SqlColumn";

	private Elements elementUtils;

	private final static Map<TypeElement, List<AnnotationMeta>> entityMap = new LinkedHashMap<>();

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		elementUtils = processingEnv.getElementUtils();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver() || annotations.isEmpty()) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Mybatis Entity Support break 1");
			return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
		}

		if (roundEnv.getRootElements() == null || roundEnv.getRootElements().isEmpty()) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Mybatis Entity Support break 2");
			return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
		}

		if (!roundEnv.processingOver()) {
			Types types = processingEnv.getTypeUtils();
			// get and process any mappers from this round
			Set<TypeElement> mappers = getMappers(annotations, roundEnv);
			mappers.forEach(typeElement -> {
				List<AnnotationMeta> fields = getFields(typeElement);
				TypeMirror superclass = typeElement.getSuperclass();
				while (superclass != null) {
					Element typesElement = types.asElement(superclass);
					if (typesElement instanceof TypeElement supper) {
						if (supper.getAnnotation(MappedSuperclass.class) == null) {
							fields.addAll(getFields(supper));
						}
						superclass = supper.getSuperclass();
					}
					else {
						superclass = null;
					}
				}
				entityMap.put(typeElement, fields);
			});
			// process entityMap
			Filer filer = processingEnv.getFiler();
			entityMap.forEach((key, value) -> writeSupportFile(filer, key, value));
			entityMap.forEach((key, value) -> writeCriteriaFile(filer, key, value));
		}
		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Mybatis Entity Support start");
		return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
	}

	private void writeSupportFile(Filer filer, TypeElement type, List<AnnotationMeta> fields) {
		// 被扫描的类的包路径
		String typeName = type.getSimpleName().toString();
		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "generate support file for:" +  typeName);
		PackageElement packageElement = elementUtils.getPackageOf(type);
		String packageName = packageElement.getQualifiedName().toString();

		TypeSpec.Builder clazzBuilder = TypeSpec.classBuilder(type.getSimpleName() + "_")
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		String uncapitalizeName = Utils.uncapitalize(typeName);

		clazzBuilder.addField(FieldSpec.builder(ClassName.get("", typeName), uncapitalizeName)
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
			.initializer("new $N()", typeName)
			.build());

		List<String> fieldsList = new ArrayList<>();

		fields.forEach(element -> {
			String fieldName = element.fieldName();
			fieldsList.add(fieldName);
			ClassName columnType = ClassName.get("org.mybatis.dynamic.sql", "SqlColumn");
			ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(columnType,
					TypeName.get(element.asType()));
			FieldSpec.Builder fieldBuilder = FieldSpec.builder(parameterizedTypeName, fieldName)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
			fieldBuilder.initializer("$N.$N", uncapitalizeName, fieldName);
			clazzBuilder.addField(fieldBuilder.build());
		});

		FieldSpec selectList = FieldSpec
			.builder(ArrayTypeName.of(ClassName.get("org.mybatis.dynamic.sql", "BasicColumn")), "selectList")
			.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
			.initializer("BasicColumn.columnList($N)", Utils.join(fieldsList, ", "))
			.build();
		clazzBuilder.addField(selectList);

		TypeSpec.Builder innerBuilder = TypeSpec.classBuilder(typeName)
			.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

		ClassName supperSqlTable = ClassName.get("org.mybatis.dynamic.sql", "AliasableSqlTable");
		innerBuilder.superclass(ParameterizedTypeName.get(supperSqlTable, TypeVariableName.get(typeName)));

		fields.forEach(element -> {
			String fieldName = element.fieldName();
			ClassName columnType = ClassName.get("org.mybatis.dynamic.sql", "SqlColumn");
			ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(columnType,
					TypeName.get(element.asType()));
			FieldSpec.Builder fieldBuilder = FieldSpec.builder(parameterizedTypeName, fieldName)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
			try {
				ColumnMeta columnMeta = getColumnMeta(element);
				fieldBuilder.initializer("column(\"$N\", $T.$N)", columnMeta.getColumnName(), JDBCType.class,
						columnMeta.getJdbcType().getName());
			}
			catch (Exception ex) {
				// handleUncaughtError(type, ex);
			}
			innerBuilder.addField(fieldBuilder.build());
		});

		String tableName = getTableName(type);
		// super("exam_auto_increment", AutoIncrement::new);
		MethodSpec constructorBuilder = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC)
			.addStatement("super(\"$N\", $N::new)", tableName, typeName)
			.build();

		innerBuilder.addMethod(constructorBuilder);

		clazzBuilder.addType(innerBuilder.build());
		TypeSpec clazz = clazzBuilder.build();

		// 创建java文件
		JavaFile javaFile = JavaFile.builder(packageName, clazz).build();
		String className = type.getQualifiedName().toString() + "_";
		try (Writer writer = filer.createSourceFile(className, type).openWriter()) {
			javaFile.writeTo(writer);
			writer.flush();
		}
		catch (IOException e) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "generate support file error:" +  e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 写入查询文件
	 * @param filer
	 * @param type
	 * @param fields
	 */
	private void writeCriteriaFile(Filer filer, TypeElement type, List<AnnotationMeta> fields) {
		// 被扫描的类的包路径
		String typeName = type.getSimpleName().toString();
		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "generate criteria file for:" +  typeName);
		PackageElement packageElement = elementUtils.getPackageOf(type);
		String packageName = packageElement.getQualifiedName().toString();
		String criteriaPackageName = getParentPackageName(packageName) + ".criteria";

		TypeSpec.Builder clazzBuilder = TypeSpec.classBuilder(type.getSimpleName() + "Criteria")
			.addModifiers(Modifier.PUBLIC);
		clazzBuilder.addAnnotation(ClassName.get("lombok", "Data"));
		clazzBuilder.superinterfaces.add(ClassName.get("net.entframework.kernel.db.mybatis.criteria", "Criteria"));

		String uncapitalizeName = Utils.uncapitalize(typeName);

		List<String> fieldsList = new ArrayList<>();

		fields.forEach(element -> {
			String fieldName = element.fieldName();
			fieldsList.add(fieldName);
			ClassName columnType = ClassName.get("", Utils.capitalize(fieldName) + "Criterion");
			FieldSpec.Builder fieldBuilder = FieldSpec.builder(columnType, fieldName).addModifiers(Modifier.PRIVATE);
			// fieldBuilder.initializer("$N.$N", uncapitalizeName, fieldName);
			clazzBuilder.addField(fieldBuilder.build());
		});

		ClassName queryExpressionDSL = ClassName.get("java.util", "List");
		ParameterizedTypeName parameterizedQueryExpressionDSL = ParameterizedTypeName.get(queryExpressionDSL,
				ClassName.get("org.mybatis.dynamic.sql", "AndOrCriteriaGroup"));

		ClassName criteriaBuilder = ClassName.get("net.entframework.kernel.db.mybatis.criteria", "CriteriaBuilder");
		MethodSpec.Builder buildBuilder = MethodSpec.methodBuilder("buildQuery")
			.addModifiers(Modifier.PUBLIC)
			.returns(parameterizedQueryExpressionDSL)
			.addStatement("return $T.buildQuery($N)", criteriaBuilder, Utils.join(fieldsList, ","));
		clazzBuilder.addMethod(buildBuilder.build());

		fields.forEach(element -> {
			String fieldName = element.fieldName();

			TypeSpec.Builder innerBuilder = TypeSpec.classBuilder(Utils.capitalize(fieldName) + "Criterion")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
			innerBuilder.addAnnotation(ClassName.get("lombok", "Data"));
			ClassName supperClassName = ClassName.get("net.entframework.kernel.db.mybatis.criteria", "FieldCriterion");
			ParameterizedTypeName supperClassTypeName = ParameterizedTypeName.get(supperClassName,
					TypeName.get(element.asType()));
			innerBuilder.superclass(supperClassTypeName);

			ClassName columnType = ClassName.get("org.mybatis.dynamic.sql", "SqlColumn");
			ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(columnType,
					TypeName.get(element.asType()));
			FieldSpec.Builder fieldBuilder = FieldSpec.builder(parameterizedTypeName, "column")
				.addModifiers(Modifier.PRIVATE);
			ClassName supportType = ClassName.get(packageName, typeName + "_");
			fieldBuilder.initializer("$T.$N", supportType, fieldName);

			innerBuilder.addField(fieldBuilder.build());

			ColumnMeta columnMeta = getColumnMeta(element);

			List<String> conditions = getConditions(element, columnMeta);
			ClassName singleValueCriterionType = ClassName.get("net.entframework.kernel.db.mybatis.criteria",
					"SingleValueCriterion");
			ClassName listValueCriterionType = ClassName.get("net.entframework.kernel.db.mybatis.criteria",
					"ListValueCriterion");
			for (String condition : conditions) {
				// boolean isListCondition = isListCondition(condition);
				// ParameterizedTypeName conditionParameterizedTypeName =
				// ParameterizedTypeName.get(isListCondition ? listValueCriterionType :
				// singleValueCriterionType,
				// ("NotNull".equals(condition) || "IsNull".equals(condition)) ?
				// ClassName.get("java.lang", "Boolean") :
				// TypeName.get(element.asType()));

				boolean isListCondition = isListCondition(condition);
				ClassName className = ClassName.get("net.entframework.kernel.db.mybatis.criteria",
						"ConditionCriterion");
				ParameterizedTypeName conditionParameterizedTypeName = ParameterizedTypeName
					.get(className.nestedClass(condition), ("NotNull".equals(condition) || "IsNull".equals(condition))
							? ClassName.get("java.lang", "Boolean") : TypeName.get(element.asType()));

				FieldSpec.Builder conditionFieldBuilder = FieldSpec
					.builder(conditionParameterizedTypeName, Utils.uncapitalize(condition))
					.addModifiers(Modifier.PRIVATE);
				ClassName subConditionType = ClassName.get("net.entframework.kernel.db.mybatis.criteria",
						"ConditionCriterion", condition);
				// conditionFieldBuilder.initializer("addCriterion(new $T<>())",
				// subConditionType);
				innerBuilder.addField(conditionFieldBuilder.build());
			}

			// 增加方法
			ParameterizedTypeName parameterizedCriterionList = ParameterizedTypeName.get(queryExpressionDSL,
					ClassName.get("net.entframework.kernel.db.mybatis.criteria", "Criterion"));

			MethodSpec.Builder criterionBuilder = MethodSpec.methodBuilder("getCriterions")
				.addModifiers(Modifier.PUBLIC)
				.returns(parameterizedCriterionList)
				.addStatement("return $T.asList($N)", ClassName.get("java.util", "Arrays"),
						Utils.join(conditions.stream().map(Utils::uncapitalize).toList(), ","));
			innerBuilder.addMethod(criterionBuilder.build());

			clazzBuilder.addType(innerBuilder.build());
		});

		TypeSpec clazz = clazzBuilder.build();

		// 创建java文件
		JavaFile javaFile = JavaFile.builder(criteriaPackageName, clazz).build();
		String className = criteriaPackageName + "." + type.getSimpleName() + "Criteria";
		try (Writer writer = filer.createSourceFile(className, type).openWriter()) {
			javaFile.writeTo(writer);
			writer.flush();
		}
		catch (IOException e) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "generate criteria file error:" +  e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean isListCondition(String condition) {
		return "Between".equals(condition) || "NotBetween".equals(condition) || "IsIn".equals(condition)
				|| "IsNotIn".equals(condition);
	}

	private List<String> getConditions(AnnotationMeta element, ColumnMeta columnMeta) {
		List<String> results = new ArrayList<>();
		// common condition
		results.add("EqualsTo");
		results.add("NotEqualTo");
		results.add("NotNull");
		results.add("IsNull");
		if (Utils.isStringField(element, columnMeta)) {
			results.add("Like");
			results.add("LikeCaseInsensitive");
			results.add("NotLike");
			results.add("NotLikeCaseInsensitive");
		}
		else if (Utils.isNumberField(element, columnMeta)) {
			results.add("Between");
			results.add("GreaterThan");
			results.add("GreaterThanOrEqualTo");
			results.add("LessThan");
			results.add("LessThanOrEqualTo");
			results.add("NotBetween");
			results.add("IsIn");
			results.add("IsNotIn");
		}
		else if (Utils.isDateField(element, columnMeta)) {
			results.add("Between");
			results.add("GreaterThan");
			results.add("GreaterThanOrEqualTo");
			results.add("LessThan");
			results.add("LessThanOrEqualTo");
			results.add("NotBetween");
			results.add("IsIn");
			results.add("IsNotIn");
		}
		return results;
	}

	private String getParentPackageName(String packageName) {
		if (packageName != null) {
			int k = packageName.lastIndexOf(".");
			return packageName.substring(0, k);
		}
		return null;
	}

	private ColumnMeta getColumnMeta(AnnotationMeta element) {
		ColumnMeta columnMeta = new ColumnMeta();
		Map<String, Object> columnData = element.getAnnotationData(PERSISTENCE_COLUMN_NAME);
		if (columnData != null) {
			String name = (String) columnData.getOrDefault("name", element.fieldName());
			columnMeta.setColumnName(name);
		}
		Map<String, Object> sqlColumnData = element.getAnnotationData(SQL_COLUMN_NAME);
		if (sqlColumnData != null) {
			JDBCType jdbcType = (JDBCType) sqlColumnData.get("jdbcType");
			columnMeta.setJdbcType(jdbcType);
		}
		else {
			throw new RuntimeException("Can't get jdbcType from :" + element.fieldName());
		}
		return columnMeta;
	}

	private String getTableName(TypeElement type) {
		String result = type.getSimpleName().toString();
		Table table = type.getAnnotation(Table.class);
		if (table != null) {
			return table.name();
		}
		return result;
	}

	private List<AnnotationMeta> getFields(TypeElement typeElement) {
		List<AnnotationMeta> results = new ArrayList<>();
		List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
		enclosedElements.forEach(element -> {
			if (element.getKind() == ElementKind.FIELD && !element.getModifiers().contains(Modifier.STATIC)
					&& element instanceof VariableElement variableElement) {
				AnnotationMeta annotationMeta = new AnnotationMeta(variableElement);

				if (annotationMeta.hasAnnotation(PERSISTENCE_COLUMN_NAME)
						&& annotationMeta.hasAnnotation(SQL_COLUMN_NAME)) {
					results.add(annotationMeta);
				}
			}
		});
		return results;
	}

	private Set<TypeElement> getMappers(final Set<? extends TypeElement> annotations,
			final RoundEnvironment roundEnvironment) {
		Set<TypeElement> mapperTypes = new HashSet<>();

		for (Element annotation : roundEnvironment.getElementsAnnotatedWith(Entity.class)) {
			TypeElement mapperTypeElement = asTypeElement(annotation);
			if (mapperTypeElement != null) {
				mapperTypes.add(mapperTypeElement);
			}
		}
		return mapperTypes;
	}

	private TypeElement asTypeElement(Element element) {
		return element.accept(new ElementKindVisitor6<TypeElement, Void>() {
			@Override
			public TypeElement visitTypeAsInterface(TypeElement e, Void p) {
				return e;
			}

			@Override
			public TypeElement visitTypeAsClass(TypeElement e, Void p) {
				return e;
			}

		}, null);
	}

	private void handleUncaughtError(Element element, Throwable thrown) {
		StringWriter sw = new StringWriter();
		thrown.printStackTrace(new PrintWriter(sw));

		String reportableStacktrace = sw.toString();
		// .replace(System.lineSeparator(), " ");

		processingEnv.getMessager()
			.printMessage(Diagnostic.Kind.ERROR, "Internal error in the mybatis processor: " + reportableStacktrace,
					element);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton("jakarta.persistence.Entity");
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

}
