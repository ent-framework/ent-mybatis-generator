package org.mybatis.generator.config;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class JoinEntry {

	private String leftTable;

	private List<Pair<String, JoinTarget>> details = new ArrayList<>();

	private List<JoinTable> joinTables = new ArrayList<>();

	public JoinEntry(String leftTable) {
		this.leftTable = leftTable;
	}

	public static String getJoinResultMapId(String javaTableName) {
		return "Join" + javaTableName + "Result";
	}

	public void validate() {
		if (isEmpty(leftTable)) {
			throw new RuntimeException("The left table participating in join operation cannot be empty");
		}
		for (Pair<String, JoinTarget> detail : details) {
			String leftTableColumn = detail.getLeft();
			if (isEmpty(leftTableColumn)) {
				throw new RuntimeException("The column of left table participating in join operation cannot be empty");
			}
			detail.getRight().validate();
		}
	}

	private boolean isEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	public String getLeftTable() {
		return leftTable;
	}

	public void setLeftTable(String leftTable) {
		this.leftTable = leftTable;
	}

	public List<Pair<String, JoinTarget>> getDetails() {
		return details;
	}

	public void setDetails(List<Pair<String, JoinTarget>> details) {
		this.details = details;
	}

	public List<JoinTable> getJoinTables() {
		return joinTables;
	}

}
