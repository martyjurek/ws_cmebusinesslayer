package com.misys.cmeobject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.misys.definitions.CMEObjectColumnDef;
import com.misys.definitions.CMEObjectDef;

public class CMEObject {

	private String type;
	private Map<String, Object> values;
	private Map<String, Object> dirtyValues;
	
	public CMEObject(String type) {
		this(type, null);
	}
	
	public CMEObject(String type, Map<String, Object> values) {
		this.type = type;
		this.values = new HashMap<String, Object>();
		if (values != null) {
			this.values.putAll(values);
		}
		this.dirtyValues = new HashMap<String, Object>();
	}
	
	public String getType() {
		return this.type;
	}
	
	protected Map<String, Object> getKeys() {
		Map<String, Object> map = new HashMap<String, Object>();
		for (CMEObjectColumnDef keyColumn : CMEObjectDef.getCmeObjDef(this.getType()).getKeyColumns()) {
			map.put(keyColumn.getColumnName(), this.values.get(keyColumn.getColumnName()));
		}
		return map;
	}
	
	public Object getValue(String key) {
		Object value = this.dirtyValues.get(key);
		if (value == null) {
			value = this.values.get(key);
		}
		return value;
	}
	
	protected void setInitialValue(String key, Object value) {
		this.values.put(key, value);
	}
	
	public void setValue(String key, Object value) {
		this.dirtyValues.put(key, value);
	}
	
	public void setValues(Map<String, Object> values) {
		this.dirtyValues.putAll(values);
	}
	
	protected Map<String, Object> getInitialValues() {
		return Collections.unmodifiableMap(this.values);
	}
	
	public Map<String, Object> getValues() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(this.values);
		map.putAll(this.dirtyValues);
		/*
		for (CMEObjectColumnDef visibleColumn : CMEObjectDef.getCmeObjDef(this.type).getVisibleColumns()) {
			System.out.println(visibleColumn.getColumnName());
			if (!map.containsKey(visibleColumn.getColumnName())) {
				System.out.println(visibleColumn.getColumnName() + " not found");
				map.remove(visibleColumn.getColumnName());
			}
		}
		*/
		return Collections.unmodifiableMap(map);
	}
	
	protected boolean isDirty() {
		return this.dirtyValues.size() > 0;
	}
	
	protected Map<String, Object> getDirtyValues() {
		return this.dirtyValues;
	}
	
	public void reset() {
		this.dirtyValues.clear();
	}
	
	protected void finalizeDirtyValues() {
		this.values.putAll(this.dirtyValues);
	}
	
}
