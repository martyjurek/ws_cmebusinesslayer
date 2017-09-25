package com.misys.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.misys.enums.CMEObjectType;

public class CMEObjectChildDef {
	
	private String childName;
	private String refColName;
	private CMEObjectType type;
	private boolean hideParent;
	List<CMEChildRelKeysDef> keys;
	
	public CMEObjectChildDef(String childName, String refColName, CMEObjectType type, boolean hideParent) {
		this.childName = childName;
		this.refColName = refColName;
		this.type = type;
		this.hideParent = hideParent;
		this.keys = new ArrayList<CMEChildRelKeysDef>();
	}

	public String getChildName() {
		return this.childName;
	}
	
	public String getRefColName() {
		return this.refColName;
	}

	public CMEObjectType getType() {
		return this.type;
	}

	public boolean isHideParent() {
		return this.hideParent;
	}
	
	public void addKey(CMEChildRelKeysDef key) {
		this.keys.add(key);
	}

	public List<CMEChildRelKeysDef> getKeys() {
		return Collections.unmodifiableList(this.keys);
	}

}
