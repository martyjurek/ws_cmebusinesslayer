package com.misys.enums;

public enum CMEObjectType {
	SINGLE("S"), GROUP("G");
	
	private String code;
	
	CMEObjectType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public static CMEObjectType getCMEObjectTypeByCode(String code) {
		for (CMEObjectType cmeObjectType : CMEObjectType.values()) {
			if (cmeObjectType.getCode().equals(code)) {
				return cmeObjectType;
			}
		}
		return null;
	}
}
