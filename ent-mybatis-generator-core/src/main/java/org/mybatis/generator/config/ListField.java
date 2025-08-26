package org.mybatis.generator.config;

import java.util.ArrayList;
import java.util.List;

public class ListField extends LimitDisplayField {

	private List<String> enumSwitches = new ArrayList<>();

	public List<String> getEnumSwitches() {
		return enumSwitches;
	}

	public void setEnumSwitches(List<String> enumSwitches) {
		this.enumSwitches = enumSwitches;
	}

}
