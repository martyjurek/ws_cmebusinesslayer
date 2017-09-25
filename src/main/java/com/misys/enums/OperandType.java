package com.misys.enums;

public enum OperandType {
	COLUMN("C"), LITERAL_STRING("L"), INPUT("I");
	
	private String code;
	
	OperandType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static OperandType getOperandTypeByCode(String code) {
		for (OperandType operandType : OperandType.values()) {
			if (operandType.getCode().equals(code)) {
				return operandType;
			}
		}
		return null;
	}
}
