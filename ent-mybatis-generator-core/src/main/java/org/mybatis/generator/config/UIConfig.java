package org.mybatis.generator.config;

public class UIConfig {

	/**
	 * 展示方式 master: list-detail single: 单页配置 默认master
	 */
	private String viewType = "Master";

	/**
	 * 下拉展示Label字段，不设置则去主键
	 */
	private String displayField;

	/**
	 * 设置查询字段，简单模式
	 */
	private LimitDisplayField searchable;

	/**
	 * 高级模式
	 */
	private LimitDisplayField criteria;

	/**
	 * 列表展示字段
	 */
	private LimitDisplayField listFields;

	/**
	 * 输入字段
	 */
	private LimitDisplayField inputFields;

	public String getViewType() {
		return viewType;
	}

	public void setViewType(String viewType) {
		this.viewType = viewType;
	}

	public String getDisplayField() {
		return displayField;
	}

	public void setDisplayField(String displayField) {
		this.displayField = displayField;
	}

	public LimitDisplayField getSearchable() {
		return searchable;
	}

	public void setSearchable(LimitDisplayField searchable) {
		this.searchable = searchable;
	}

	public LimitDisplayField getCriteria() {
		return criteria;
	}

	public void setCriteria(LimitDisplayField criteria) {
		this.criteria = criteria;
	}

	public LimitDisplayField getListFields() {
		return listFields;
	}

	public void setListFields(LimitDisplayField listFields) {
		this.listFields = listFields;
	}

	public LimitDisplayField getInputFields() {
		return inputFields;
	}

	public void setInputFields(LimitDisplayField inputFields) {
		this.inputFields = inputFields;
	}

}
