package net.entframework.kernel.db.generator.typescript;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import net.entframework.kernel.db.generator.typescript.runtime.FullyQualifiedTypescriptType;
import net.entframework.kernel.db.generator.typescript.runtime.TypescriptTopLevelClass;
import net.entframework.kernel.db.generator.utils.ClassInfo;
import net.entframework.kernel.db.generator.utils.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.ListField;
import org.mybatis.generator.config.UIConfig;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * 生成typescript model
 */
public class TypescriptModelGenerator extends AbstractJavaGenerator {

	public TypescriptModelGenerator(String project) {
		super(project);
	}

	private void prepare() {

	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {

		String projectRootAlias = this.context.getProperty("projectRootAlias");
		if (StringUtils.isBlank(projectRootAlias)) {
			projectRootAlias = "";
		}
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.8", table.toString())); //$NON-NLS-1$
		Plugin plugins = context.getPlugins();
		CommentGenerator commentGenerator = context.getCommentGenerator();

		String camelCaseName = JavaBeansUtil.convertCamelCase(table.getDomainObjectName(), "-");
		String typescriptModelPackage = this.context.getJavaModelGeneratorConfiguration().getTargetPackage();
		FullyQualifiedTypescriptType tsBaseModelJavaType = new FullyQualifiedTypescriptType(projectRootAlias,
				typescriptModelPackage + "." + camelCaseName + "." + table.getDomainObjectName(), true);

		TypescriptTopLevelClass topLevelClass = new TypescriptTopLevelClass(tsBaseModelJavaType);
		introspectedTable.setBaseModelClass(topLevelClass);

		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		topLevelClass.setDescription(this.introspectedTable.getRemarks());

		commentGenerator.addJavaFileComment(topLevelClass);

		List<CompilationUnit> answer = new ArrayList<>();

		commentGenerator.addModelClassComment(topLevelClass, introspectedTable);

		List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();

		List<String> enumSwitches = new ArrayList<>();
		UIConfig uiConfig = introspectedTable.getTableConfiguration().getUiConfig();
		if (uiConfig != null && uiConfig.getListFields() != null) {
			ListField listFields = uiConfig.getListFields();
			if (!listFields.getEnumSwitches().isEmpty()) {
				enumSwitches.addAll(listFields.getEnumSwitches());
			}
		}

		for (IntrospectedColumn introspectedColumn : introspectedColumns) {

			Field field = WebUtils.getTypescriptField(introspectedColumn, context, introspectedTable, topLevelClass);

			String remarks = introspectedColumn.getRemarks();
			if (StringUtils.isEmpty(remarks)) {
				remarks = field.getName();
			}
			field.setDescription(remarks);
			GeneratorUtils.addComment(field, remarks);

			// 检查类型是否枚举类型
			ClassInfo classInfo = ClassInfo
				.getInstance(introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName());
			if (classInfo != null && classInfo.isEnum()) {
				if ("net.entframework.kernel.core.enums.StatusEnum"
					.equals(classInfo.getJavaType().getFullyQualifiedName())) {
					if (enumSwitches.contains(field.getName())) {
						field.setAttribute(Constants.FIELD_ENUM_SWITCH_ATTR, "Status");
					}
					else {
						field.setAttribute(Constants.FIELD_ENUM_LABEL_ATTR, "Status");
					}
				}
				if ("net.entframework.kernel.core.enums.YesOrNotEnum"
					.equals(classInfo.getJavaType().getFullyQualifiedName())) {
					if (enumSwitches.contains(field.getName())) {
						field.setAttribute(Constants.FIELD_ENUM_SWITCH_ATTR, "YesOrNot");
					}
					else {
						field.setAttribute(Constants.FIELD_ENUM_LABEL_ATTR, "YesOrNot");
					}
				}
				String enumPackage = typescriptModelPackage + ".enum";
				TopLevelEnumeration topLevelEnumeration = classInfo.toTopLevelEnumeration(enumPackage,
						introspectedColumn.getFullyQualifiedJavaType().getShortName(), projectRootAlias);
				topLevelEnumeration.setWriteMode(WriteMode.OVER_WRITE);
				FullyQualifiedJavaType fqjt = topLevelEnumeration.getType();
				field.setType(fqjt);
				field.setAttribute(Constants.TABLE_ENUM_FIELD_ATTR, true);
				answer.add(topLevelEnumeration);
			}

			if (StringUtils.equalsIgnoreCase(introspectedColumn.getActualColumnName(),
					introspectedTable.getTableConfiguration().getLogicDeleteColumn())) {
				field.setAttribute(Constants.FIELD_LOGIC_DELETE_ATTR, true);
			}

			if (StringUtils.equalsIgnoreCase(introspectedColumn.getActualColumnName(),
					introspectedTable.getTableConfiguration().getVersionColumn())) {
				field.setAttribute(Constants.FIELD_VERSION_ATTR, true);
			}

			if (StringUtils.equalsIgnoreCase(introspectedColumn.getActualColumnName(),
					introspectedTable.getTableConfiguration().getTenantColumn())) {
				field.setAttribute(Constants.FIELD_TENANT_ATTR, true);
			}

			if (plugins.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable,
					Plugin.ModelClassType.BASE_RECORD)) {
				topLevelClass.addField(field);
				topLevelClass.addImportedType(field.getType());
			}
		}

		if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {

			InitializationBlock initializationBlock = new InitializationBlock();
			initializationBlock.addBodyLine(String.format("export type %sPageModel = BasicFetchResult<%s>;",
					table.getDomainObjectName(), table.getDomainObjectName()));
			topLevelClass
				.addImportedType(new FullyQualifiedTypescriptType("", "fe-ent-core.es.logics.BasicFetchResult", true));
			topLevelClass.addInitializationBlock(initializationBlock);

			answer.add(topLevelClass);
		}

		return answer;
	}

}
