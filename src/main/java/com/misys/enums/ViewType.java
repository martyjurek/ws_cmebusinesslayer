package com.misys.enums;

public enum ViewType {
	SELECT("S"), UPDATE("U"), IDENTITY_INSERT("R"), INSERT("I"), DELETE("D");
	
	private String code;
	
	ViewType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static ViewType getViewTypeByCode(String code) {
		for (ViewType viewType : ViewType.values()) {
			if (viewType.getCode().equals(code)) {
				return viewType;
			}
		}
		return null;
	}
	
}
