package org.mybatis.generator.config;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class SearchableColumn extends PropertyHolder {

	private final List<String> columnNames = new ArrayList<>();

	public SearchableColumn(String columnNames) {
		String[] splitColumns = StringUtils.split(columnNames, ",");
		if (splitColumns != null) {
			for (String column : splitColumns) {
				if (StringUtils.isNotBlank(column)) {
					this.columnNames.add(StringUtils.trim(column));
				}
			}
		}
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

}
