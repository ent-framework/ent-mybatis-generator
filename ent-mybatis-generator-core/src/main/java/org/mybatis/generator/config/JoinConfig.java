package org.mybatis.generator.config;

import java.util.HashMap;
import java.util.Map;

public class JoinConfig {

	private Map<String, JoinEntry> joinDetailMap = new HashMap<>();

	public JoinConfig() {
	}

	public JoinEntry getJoinEntry(String tableName) {
		return joinDetailMap.get(tableName);
	}

	public Map<String, JoinEntry> getJoinDetailMap() {
		return joinDetailMap;
	}

	public void setJoinDetailMap(Map<String, JoinEntry> joinDetailMap) {
		this.joinDetailMap = joinDetailMap;
	}

}
