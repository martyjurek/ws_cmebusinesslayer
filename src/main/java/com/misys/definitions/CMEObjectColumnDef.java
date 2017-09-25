package com.misys.definitions;

import com.misys.enums.DataType;

public class CMEObjectColumnDef {

	private String columnName;
	private DataType dataType;
	private boolean key;
	private boolean required;
	private boolean visible;
	private String defaultValue;
	private String refObjectName;
	private String refColumnName;
	
	public CMEObjectColumnDef(String columnName, DataType dataType, boolean key, boolean required, 
			boolean visible, String defaultValue, String refObjectName, String refColumnName) {
		
		this.columnName = columnName;
		this.dataType = dataType;
		this.key = key;
		this.required = required;
		this.visible = visible;
		this.defaultValue = defaultValue;
		this.refObjectName = refObjectName;
		this.refColumnName = refColumnName;
	}
	
	public String getColumnName() {
		return this.columnName;
	}
	
	public DataType getDataType() {
		return this.dataType;
	}
	
	public boolean isKey() {
		return this.key;
	}
	
	public boolean isRequired() {
		return this.required;
	}
	
	public boolean isVisible() {
		return this.visible;
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public String getRefObjectName() {
		return this.refObjectName;
	}
	
	public String getRefColumnName() {
		return this.refColumnName;
	}
	
	/*
	public boolean isReferenceColumn() {
		if (this.refObjectName != null && this.refColumnName != null) {
			return true;
		}
		return false;
	}
	*/
	
	@Override
	public int hashCode() {
		return this.columnName.hashCode();
	}
	
}
