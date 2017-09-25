package com.misys.jdbc;

import com.misys.enums.DataType;

public class DBColumn {

	private String columnName;		  
	private String columnTableName;		  
	private DataType dataType;		  
	private String defaultValue;    		  
	private Integer maxStringLength;
	private Integer precision;  		  
	private Integer scale;  		  		
	private Integer ordinalPosition;	    		  
	private boolean isNullable;   		  
	private boolean isAutoIncrement;	    		  
	private boolean isPrimaryKey;	    		  
	private boolean isForeignKey;	    		  
		
	public DBColumn() {}
	
	public String getColumnInfo() {
		return
		   "\nCOLUMN_NAME: "+ getColumnName()
		   + "\n   TABLE_NAME: " + getColumnTableName()			  	           
		   + "\n   DATA_TYPE: " + getDataType()
		   + "\n   MAX_STRING_LENGTH: " + getMaxStringLength()
		   + "\n   PRECISION: " + getPrecision()
		   + "\n   SCALE: " + getScale()
		   + "\n   IS_NULLABLE: " + isNullable()
		   + "\n   COLUMN_DEFAULT: " + getDefaultValue()
		   + "\n   IS_AUTOINCREMENT: " + isAutoIncrement()
		   + "\n   IS_PRIMARY_KEY: " + isPrimaryKey()
		   + "\n   IS_FOREIGN_KEY: " + isForeignKey()
		   + "\n   ORDINAL_POSITION: " + getOrdinalPosition(); 		    	  				
	}

	public String getColumnName() {
		return this.columnName;
	}

	protected void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnTableName() {
		return this.columnTableName;
	}

	protected void setColumnTableName(String columnTableName) {
		this.columnTableName = columnTableName;
	}

	public DataType getDataType() {
		return this.dataType;
	}

	protected void setDataType(DataType dataType) {
		this.dataType = dataType;
	}
	
	public Integer getMaxStringLength() {
		return this.maxStringLength;
	}
	
	protected void setMaxStringLength(Integer maxStringLength) {
		this.maxStringLength = maxStringLength;
	}
	
	public Integer getPrecision() {
		return this.precision;
	}
	
	protected void setPrecision(Integer precision) {
		this.precision = precision;
	}
	
	public Integer getScale() {
		return this.scale;
	}
	
	protected void setScale(Integer scale) {
		this.scale = scale;
	}
	
	public boolean isNullable() {
		return this.isNullable;
	}
	
	protected void setIsNullable(boolean isNullable) {
		this.isNullable = isNullable;
	}
	
	public String getDefaultValue() {
		return this.defaultValue;
	}
	
	protected void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public boolean isAutoIncrement() {
		return this.isAutoIncrement;
	}	

	protected void setIsAutoIncrement(boolean isAutoIncrement) {
		this.isAutoIncrement = isAutoIncrement;
	}	

	public boolean isPrimaryKey() {
		return this.isPrimaryKey;
	}	
	
	protected void setIsPrimaryKey(boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}	

	public boolean isForeignKey() {
		return this.isForeignKey;
	}	

	protected void setIsForeignKey(boolean isForeignKey) {
		this.isForeignKey = isForeignKey;
	}	

	public Integer getOrdinalPosition() {
		return this.ordinalPosition;
	}	

	protected void setOrdinalPosition(Integer ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
	}
}
