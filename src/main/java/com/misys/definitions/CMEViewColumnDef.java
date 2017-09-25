package com.misys.definitions;

import com.misys.enums.DataType;

public class CMEViewColumnDef {

	private String columnName;
	//private DataType dataType;
	//private boolean key;
	private String tableName;
	private String tableAlias;
	
	/*
	public CMEViewColumnDef(String columnName, DataType dataType, boolean key, String tableAlias) {
		this.columnName = columnName;
		this.dataType = dataType;
		this.key = key;
		this.tableAlias = tableAlias;
	}
	*/
	public CMEViewColumnDef(String columnName, String tableName, String tableAlias) {
		this.columnName = columnName;
		this.tableName = tableName;
		this.tableAlias = tableAlias;
	}

	public String getColumnName() {
		return this.columnName;
	}

	/*
	public DataType getDataType() {
		return this.dataType;
	}

	public boolean isKey() {
		return this.key;
	}
	*/
	
	public String getTableName() {
		return this.tableName;
	}
	
	public String getTableAlias() {
		return this.tableAlias;
	}
	
}
