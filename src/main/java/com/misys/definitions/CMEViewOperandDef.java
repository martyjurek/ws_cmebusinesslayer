package com.misys.definitions;

import com.misys.enums.DataType;
import com.misys.enums.OperandType;

public class CMEViewOperandDef {

	private OperandType operandType;
	private String columnName;
	private String literalString;
	private DataType dataType;
	
	public CMEViewOperandDef(OperandType operandType, String columnName, String literalString, DataType dataType) {
		this.operandType = operandType;
		this.columnName = columnName;
		this.literalString = literalString;
		this.dataType = dataType;
	}

	public OperandType getOperandType() {
		return this.operandType;
	}

	public String getColumnName() {
		return this.columnName;
	}

	public String getLiteralString() {
		return this.literalString;
	}

	public DataType getDataType() {
		return this.dataType;
	}
	
	@Override
	public int hashCode() {
		switch (this.operandType) {
			case COLUMN:
				return this.columnName.hashCode();
			case INPUT:
				return this.operandType.toString().hashCode();
			case LITERAL_STRING:
				return this.literalString.hashCode();
		}
		return 0;
	}
	
}
