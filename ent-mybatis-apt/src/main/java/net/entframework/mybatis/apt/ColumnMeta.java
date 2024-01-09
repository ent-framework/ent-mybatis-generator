/*
 * Copyright (c) 2024. Licensed under the Apache License, Version 2.0.
 */

package net.entframework.mybatis.apt;

import java.sql.JDBCType;

public class ColumnMeta {

	private String columnName;

	private JDBCType jdbcType;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public JDBCType getJdbcType() {
		return jdbcType;
	}

	public void setJdbcType(JDBCType jdbcType) {
		this.jdbcType = jdbcType;
	}

}
