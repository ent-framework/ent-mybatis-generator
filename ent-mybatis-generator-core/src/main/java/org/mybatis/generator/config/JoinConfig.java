package org.mybatis.generator.config;

import java.util.HashMap;
import java.util.Map;

public class JoinConfig {

	private String targetPackage;

	private String targetProject;

	private Map<String, JoinEntry> joinDetailMap = new HashMap<>();

	public JoinConfig(String targetPackage, String targetProject) {
		this.targetPackage = targetPackage;
		this.targetProject = targetProject;
	}

	public JoinEntry getJoinEntry(String tableName) {
		JoinEntry joinEntry = joinDetailMap.get(tableName);
		if (joinEntry != null) {
			joinEntry.setTargetPackage(targetPackage);
			joinEntry.setTargetProject(targetProject);
		}
		return joinEntry;
	}

	public String getTargetPackage() {
		return targetPackage;
	}

	public void setTargetPackage(String targetPackage) {
		this.targetPackage = targetPackage;
	}

	public String getTargetProject() {
		return targetProject;
	}

	public void setTargetProject(String targetProject) {
		this.targetProject = targetProject;
	}

	public Map<String, JoinEntry> getJoinDetailMap() {
		return joinDetailMap;
	}

	public void setJoinDetailMap(Map<String, JoinEntry> joinDetailMap) {
		this.joinDetailMap = joinDetailMap;
	}

}
