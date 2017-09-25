package com.misys.definitions;

import com.misys.enums.JoinType;

public class CMEViewJoinDef {
	
	private String columnName;
	private String tableAlias;
	private JoinType joinType;
	private String parentColumnName;
	private String parentTableAlias;
	
	public CMEViewJoinDef(String columnName, String tableAlias, JoinType joinType,
			String parentColumnName, String parentTableAlias) {
		
		this.columnName = columnName;
		this.tableAlias = tableAlias;
		this.joinType = joinType;
		this.parentColumnName = parentColumnName;
		this.parentTableAlias = parentTableAlias;
	}

	public String getColumnName() {
		return this.columnName;
	}

	public String getTableAlias() {
		return this.tableAlias;
	}

	public JoinType getJoinType() {
		return this.joinType;
	}

	public String getParentColumnName() {
		return this.parentColumnName;
	}

	public String getParentTableAlias() {
		return this.parentTableAlias;
	}

}
