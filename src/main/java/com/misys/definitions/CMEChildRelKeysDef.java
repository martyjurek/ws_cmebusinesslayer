package com.misys.definitions;

public class CMEChildRelKeysDef {

	private String columnName;
	private String parentColumnName;
	
	public CMEChildRelKeysDef(String columnName, String parentColumnName) {
		this.columnName = columnName;
		this.parentColumnName = parentColumnName;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getParentColumnName() {
		return parentColumnName;
	}

}
