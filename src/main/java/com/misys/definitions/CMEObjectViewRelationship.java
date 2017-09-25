package com.misys.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMEObjectViewRelationship {

	private String viewName;
	private Map<String, String> columnRelMap;
	
	public CMEObjectViewRelationship(String viewName) {
		this.viewName = viewName;
		this.columnRelMap = new HashMap<String, String>();
	}

	public String getViewName() {
		return this.viewName;
	}
	
	public void addRelationship(String cmeObjColName, String cmeViewColName) {
		this.columnRelMap.put(cmeObjColName, cmeViewColName);
	}

	public Map<String, String> getColumnRelMap() {
		return Collections.unmodifiableMap(this.columnRelMap);
	}
	
	public String getViewColName(String cmeObjColName) {
		return this.columnRelMap.get(cmeObjColName);
	}
	
	public List<String> getViewColNames(List<String> cmeObjColNames) {
		List<String> viewColNames = new ArrayList<String>();
		for (String cmeObjColName : cmeObjColNames) {
			viewColNames.add(this.columnRelMap.get(cmeObjColName));
		}
		return viewColNames;
	}
	
	public List<String> getViewColNames() {
		return new ArrayList<String>(this.columnRelMap.values());
	}

	public String getCmeObjColName(String viewColName) {
		for (Map.Entry<String, String> entry : this.columnRelMap.entrySet()) {
			if (entry.getValue().equals(viewColName)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public List<String> getCmeObjColNames(List<String> viewColNames) {
		List<String> cmeObjColNames = new ArrayList<String>();
		for (String viewColName : viewColNames) {
			String cmeObjColName = this.getCmeObjColName(viewColName);
			if (cmeObjColName != null) {
				cmeObjColNames.add(cmeObjColName);
			}
		}
		return cmeObjColNames;
	}
	
	public List<String> getCmeObjColNames() {
		return new ArrayList<String>(this.columnRelMap.keySet());
	}
	
	
}
