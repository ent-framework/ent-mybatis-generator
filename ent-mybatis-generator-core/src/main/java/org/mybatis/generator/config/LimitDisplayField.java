package org.mybatis.generator.config;

import java.util.ArrayList;
import java.util.List;

public class LimitDisplayField {

	private List<String> fields = new ArrayList<>();

	private List<String> ignored = new ArrayList<>();

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public List<String> getIgnored() {
		return ignored;
	}

	public void setIgnored(List<String> ignored) {
		this.ignored = ignored;
	}

}
