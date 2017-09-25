package com.misys.cmeobject;

import java.util.LinkedHashMap;
import java.util.Map;

public class CMEObjectChildInfo {

	private String childType;
	private String parentRefColName;
	private Map<String, Object> keyMap;
	
	public CMEObjectChildInfo() {
		this.keyMap = new LinkedHashMap<String, Object>();
	}

	public String getChildType() {
		return this.childType;
	}

	public void setChildType(String childType) {
		this.childType = childType;
	}

	public String getParentRefColName() {
		return this.parentRefColName;
	}

	public void setParentRefColName(String parentRefColName) {
		this.parentRefColName = parentRefColName;
	}

	public Map<String, Object> getKeyMap() {
		return this.keyMap;
	}
	
	public void addKey(String key, Object value) {
		this.keyMap.put(key, value);
	}
	
}
