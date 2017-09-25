package com.misys.enums;

public enum ObjectAction {
	SEARCH("S"), CREATE("C"), LOAD("L"), UPDATE("U"), DELETE("D");
	
	private String code;
	
	ObjectAction(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static ObjectAction getObjectActionByCode(String code) {
		for (ObjectAction action : ObjectAction.values()) {
			if (action.getCode().equals(code)) {
				return action;
			}
		}
		return null;
	}
	
}
