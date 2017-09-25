package com.misys.enums;

public enum JoinType {
	FULL("F"), LEFT("L"), RIGHT("R"), INNER("I");
	
	private String code;
	
	JoinType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static JoinType getJoinTypeByCode(String code) {
		for (JoinType joinType : JoinType.values()) {
			if (joinType.getCode().equals(code)) {
				return joinType;
			}
		}
		return null;
	}
}
