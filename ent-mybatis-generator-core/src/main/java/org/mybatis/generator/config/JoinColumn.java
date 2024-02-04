package org.mybatis.generator.config;

public class JoinColumn {

	private String columnName;

	private String referencedColumnName;

	public String getColumnName() {
		return columnName;
	}

	public JoinColumn setColumnName(String columnName) {
		this.columnName = columnName;
		return this;
	}

	public String getReferencedColumnName() {
		return referencedColumnName;
	}

	public JoinColumn setReferencedColumnName(String referencedColumnName) {
		this.referencedColumnName = referencedColumnName;
		return this;
	}

}
