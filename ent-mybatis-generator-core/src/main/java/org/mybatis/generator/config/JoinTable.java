package org.mybatis.generator.config;

public class JoinTable {

	private String middleTable;

	private String rightTable;

	private String property;

	private JoinColumn joinColumn;

	private JoinColumn inverseJoinColumn;

	public JoinColumn getJoinColumn() {
		return joinColumn;
	}

	public JoinTable setJoinColumn(JoinColumn joinColumn) {
		this.joinColumn = joinColumn;
		return this;
	}

	public JoinColumn getInverseJoinColumn() {
		return inverseJoinColumn;
	}

	public JoinTable setInverseJoinColumn(JoinColumn inverseJoinColumn) {
		this.inverseJoinColumn = inverseJoinColumn;
		return this;
	}

	public String getMiddleTable() {
		return middleTable;
	}

	public JoinTable setMiddleTable(String middleTable) {
		this.middleTable = middleTable;
		return this;
	}

	public String getRightTable() {
		return rightTable;
	}

	public JoinTable setRightTable(String rightTable) {
		this.rightTable = rightTable;
		return this;
	}

	public String getProperty() {
		return property;
	}

	public JoinTable setProperty(String property) {
		this.property = property;
		return this;
	}

}
