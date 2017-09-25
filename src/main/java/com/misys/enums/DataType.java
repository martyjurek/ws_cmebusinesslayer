package com.misys.enums;

public enum DataType {
	STRING("S"), INTEGER("I"), DECIMAL("D");
	
	private String code;
	
	DataType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static DataType getJoinTypeByCode(String code) {
		for (DataType dataType : DataType.values()) {
			if (dataType.getCode().equals(code)) {
				return dataType;
			}
		}
		return null;
	}
}
