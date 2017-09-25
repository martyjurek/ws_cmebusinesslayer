package com.misys.enums;

public enum OperatorType {
	EQUALS("EQ"), NOT_EQUALS("NE"), LIKE("LI"), NOT_LIKE("NL"),
	IN("IN"), NOT_IN("NI"), GREATER_THAN("GT"), GREATER_THAN_EQUALS("GE"), 
	LESS_THAN("LT"), LESS_THAN_EQUALS("LE");
	
	private String code;
	
	OperatorType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static OperatorType getOperatorTypeByCode(String code) {
		for (OperatorType operatorType : OperatorType.values()) {
			if (operatorType.getCode().equals(code)) {
				return operatorType;
			}
		}
		return null;
	}
}
