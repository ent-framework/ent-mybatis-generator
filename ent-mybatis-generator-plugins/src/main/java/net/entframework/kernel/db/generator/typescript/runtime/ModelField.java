package net.entframework.kernel.db.generator.typescript.runtime;

import net.entframework.kernel.db.generator.Constants;
import net.entframework.kernel.db.generator.config.Relation;
import net.entframework.kernel.db.generator.plugin.generator.GeneratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.JoinTarget;

import java.time.LocalDateTime;

public class ModelField {

	private final Field field;

	private final IntrospectedColumn column;

	private boolean hidden;

	private boolean isPrimaryKey;

	private String inputType;

	private String fieldType;

	public ModelField(Field field, IntrospectedColumn column) {
		this.field = field;
		this.column = column;
		this.isPrimaryKey = false;
		calc();
	}

	private void calc() {
		this.fieldType = calcFieldType();
		this.inputType = calcInputType();
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.isPrimaryKey = primaryKey;
	}

	public boolean isPrimaryKey() {
		return this.isPrimaryKey;
	}

	public FullyQualifiedJavaType getJavaType() {
		return field.getType();
	}

	public String getName() {
		return field.getName();
	}

	public String getDescription() {
		String desc = field.getDescription();
		if (StringUtils.isEmpty(desc)) {
			desc = field.getName();
		}
		return desc;
	}

	public int getLength() {
		return column.getLength();
	}

	public boolean isRequired() {
		if (this.isRelationField()) {
			if (this.isManyToOne()) {
				return !this.getRelation().getSourceColumn().isNullable();
			}
			return false;
		}
		return !column.isNullable();
	}

	public int getScale() {
		return column.getScale();
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	private String calcFieldType() {

		if (field.getAttribute(Constants.FIELD_RELATION) != null) {
			return "relation";
		}

		if (StringUtils.equalsAny(this.field.getType().getShortName(), "boolean", "Boolean")) {
			return "boolean";
		}
		if (this.field.getAttribute(Constants.TABLE_ENUM_FIELD_ATTR) != null) {
			return "enum";
		}

		if (this.column.isStringColumn()) {
			return "string";
		}
		if (this.column.isNumberColumn()) {
			return "number";
		}
		if (this.column.isClobColumn()) {
			return "clob";
		}
		if ("DATE".equals(this.column.getJdbcTypeName()) || this.column.isJDBCDateColumn()) {
			return "date";
		}
		if ("TIMESTAMP".equals(this.column.getJdbcTypeName())) {
			return "date-time";
		}
		if ("TIME".equals(this.column.getJdbcTypeName()) || this.column.isJDBCTimeColumn()) {
			return "time";
		}
		return "string";
	}

	private String calcInputType() {
		if (StringUtils.equals("number", this.fieldType)) {
			return "InputNumber";
		}
		if (StringUtils.equals("date", this.fieldType)) {
			return "DatePicker";
		}
		if (StringUtils.equals("time", this.fieldType)) {
			return "TimePicker";
		}
		if (StringUtils.equals("date-time", this.fieldType)) {
			return "DatePicker";
		}
		if (StringUtils.equals("boolean", this.fieldType)) {
			return "Switch";
		}
		if (StringUtils.equals("enum", this.fieldType)) {
			return "Select";
		}
		if (StringUtils.equals("relation", this.fieldType)) {
			return "ApiSelect";
		}
		if (StringUtils.equals("clob", this.fieldType)) {
			return "InputTextArea";
		}
		if (StringUtils.equals("string", this.fieldType) && this.column.getLength() >= 400) {
			return "InputTextArea";
		}
		return "Input";
	}

	public int getTextLength() {
		return this.column.getLength();
	}

	public String getInputType() {
		return inputType;
	}

	public String getFieldType() {
		return fieldType;
	}

	public String getRemarks() {
		return column.getRemarks();
	}

	public String getDefaultValue() {
		return column.getDefaultValue();
	}

	public boolean isLogicDeleteField() {
		return GeneratorUtils.isLogicDeleteField(field);
	}

	public boolean isEnumField() {
		return field.getAttribute(Constants.TABLE_ENUM_FIELD_ATTR) != null;
	}

	public boolean isVersionField() {
		return GeneratorUtils.isVersionField(field);
	}

	public boolean isTenantField() {
		return GeneratorUtils.isTenantField(field);
	}

	public boolean isBlob() {
		return column.isBLOBColumn();
	}

	public boolean isBasic() {
		return field.getAttribute(Constants.FIELD_EXT_ATTR) == null;
	}

	public boolean isRelationField() {
		return GeneratorUtils.isRelationField(this.field);
	}

	public boolean isManyToOne() {
		return isRelationType(JoinTarget.JoinType.MANY_TO_ONE);
	}

	public boolean isOneToMany() {
		return isRelationType(JoinTarget.JoinType.ONE_TO_MANY);
	}

	public boolean isManyToMany() {
		return isRelationType(JoinTarget.JoinType.MANY_TO_MANY);
	}

	public boolean isEnumLabel() {
		return field.getAttribute(Constants.FIELD_ENUM_LABEL_ATTR) != null;
	}

	public String getEnumLabelType() {
		return (String) field.getAttribute(Constants.FIELD_ENUM_LABEL_ATTR);
	}

	public boolean isEnumSwitch() {
		return field.getAttribute(Constants.FIELD_ENUM_SWITCH_ATTR) != null;
	}

	public String getEnumSwitchType() {
		return (String) field.getAttribute(Constants.FIELD_ENUM_SWITCH_ATTR);
	}

	private boolean isRelationType(JoinTarget.JoinType joinType) {
		Relation relation = (Relation) field.getAttribute(Constants.FIELD_RELATION);
		return relation != null && relation.getJoinType() != null && relation.getJoinType() == joinType;
	}

	public Relation getRelation() {
		return (Relation) field.getAttribute(Constants.FIELD_RELATION);
	}

	public static ModelField copy(ModelField source) {
		ModelField result = new ModelField(source.field, source.column);
		result.setPrimaryKey(source.isPrimaryKey);
		return result;
	}

}
