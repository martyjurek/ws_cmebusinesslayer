package com.misys.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.misys.enums.ObjectAction;

public class CMEObjectDef {

	private static Map<String, CMEObjectDef> cmeObjects = new HashMap<String, CMEObjectDef>();
	private static Map<String, String> pathToObjectName = new HashMap<>();
	
	/**
	 * Adds the given CMEObjectDef to the global collection of definitions.
	 * @param cmeObjDef CMEObject definition to add to the global collection of definitions.
	 */
	public static void addCmeObjDef(CMEObjectDef cmeObjDef) {
		CMEObjectDef.cmeObjects.put(cmeObjDef.getName(), cmeObjDef);
		CMEObjectDef.pathToObjectName.put(cmeObjDef.getPath(), cmeObjDef.getName());
	}
	
	/**
	 * Gets the CMEObjectDef from the global collection of definitions with the given name.
	 * @param name CMEObject name to get the definition for.
	 * @return The CMEObjectDef for the CMEObject with the given name.
	 * @note This is equivalent to CMEObjectDef.getCMEObjectDefs().get(name).
	 */
	public static CMEObjectDef getCmeObjDef(String name) {
		return CMEObjectDef.cmeObjects.get(name);
	}
	
	/**
	 * Gets the CMEObject name from the global collection of definitions with the given path.
	 * @param path CMEObject path to get the definition for.
	 * @return name for the CMEObject with the given path.
	 */
	public static String getCmeObjNameByPath(String path) {
	    return pathToObjectName.get(path);
	}
	
	/**
	 * Gets the collection of all loaded CMEObject definitions.
	 * The collection is a map from CMEObject names to their definitions.
	 * @return A map of all CMEObject definitions.
	 * @note CMEObject names are defined in t_ccs_admin_cme_object.
	 */
	public static Map<String, CMEObjectDef> getCMEObjectDefs() {
		return Collections.unmodifiableMap(CMEObjectDef.cmeObjects);
	}
	
	private String name;
	private String path;
	private List<CMEObjectColumnDef> columns;
	private List<CMEObjectViewRelationship> searchViews;
	private List<CMEObjectViewRelationship> loadViews;
	private List<CMEObjectViewRelationship> updateViews;
	private List<CMEObjectViewRelationship> createViews;
	private List<CMEObjectViewRelationship> deleteViews;
	private List<CMEObjectChildDef> childDefs;
	
	public CMEObjectDef(String name, String path) {
		this.name = name;
		this.path = path;
		this.columns = new ArrayList<CMEObjectColumnDef>();
		this.searchViews = new ArrayList<CMEObjectViewRelationship>();
		this.loadViews = new ArrayList<CMEObjectViewRelationship>();
		this.updateViews = new ArrayList<CMEObjectViewRelationship>();
		this.createViews = new ArrayList<CMEObjectViewRelationship>();
		this.deleteViews = new ArrayList<CMEObjectViewRelationship>();
		this.childDefs = new ArrayList<CMEObjectChildDef>();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public void addColumn(CMEObjectColumnDef column) {
		this.columns.add(column);
	}

	public List<CMEObjectColumnDef> getColumns() {
		return Collections.unmodifiableList(this.columns);
	}
	
	public CMEObjectColumnDef getColumnByName(String columnName) {
		for (CMEObjectColumnDef column : this.columns) {
			if (column.getColumnName().equals(columnName)) {
				return column;
			}
		}
		return null;
	}
	
	public List<CMEObjectColumnDef> getColumnsByNames(List<String> columnNames) {
		List<CMEObjectColumnDef> columns = new ArrayList<CMEObjectColumnDef>();
		for (String columnName : columnNames) {
			for (CMEObjectColumnDef column : this.columns) {
				if (column.getColumnName().equals(columnName)) {
					columns.add(column);
				}
			}
		}
		return columns;
	}
	
	public List<CMEObjectColumnDef> getKeyColumns() {
		List<CMEObjectColumnDef> keys = new ArrayList<CMEObjectColumnDef>();
		for (CMEObjectColumnDef column : this.columns) {
			if (column.isKey()) {
				keys.add(column);
			}
		}
		return keys;
	}
	
	public List<CMEObjectColumnDef> getNonKeyColumns() {
		List<CMEObjectColumnDef> keys = new ArrayList<CMEObjectColumnDef>();
		for (CMEObjectColumnDef column : this.columns) {
			if (!column.isKey()) {
				keys.add(column);
			}
		}
		return keys;
	}
	
	/*
	public List<CMEObjectColumnDef> getReferenceColumns() {
		List<CMEObjectColumnDef> refColumns = new ArrayList<CMEObjectColumnDef>();
		for (CMEObjectColumnDef column : this.columns) {
			if (column.isReferenceColumn()) {
				refColumns.add(column);
			}
		}
		return refColumns;
	}
	*/
	
	public List<CMEObjectColumnDef> getVisibleColumns() {
		List<CMEObjectColumnDef> refColumns = new ArrayList<CMEObjectColumnDef>();
		for (CMEObjectColumnDef column : this.columns) {
			if (column.isVisible()) {
				refColumns.add(column);
			}
		}
		return refColumns;
	}
	
	public Map<String, Object> getDefaultValues() {
		Map<String, Object> map = new HashMap<String, Object>();
		for (CMEObjectColumnDef column : this.columns) {
			if (column.getDefaultValue() != null) {
				map.put(column.getColumnName(), column.getDefaultValue());
			}
		}
		return map;
	}
	
	public void addView(ObjectAction action, CMEObjectViewRelationship view) {
		switch (action) {
			case SEARCH:
				this.searchViews.add(view);
				break;
			case LOAD:
				this.loadViews.add(view);
				break;
			case CREATE:
				this.createViews.add(view);
				break;
			case DELETE:
				this.deleteViews.add(view);
				break;
			case UPDATE:
				this.updateViews.add(view);
				break;
		}
	}
	
	public List<CMEObjectViewRelationship> getViews(ObjectAction action) {
		switch (action) {
			case SEARCH:
				return this.searchViews;
			case LOAD:
				return this.loadViews;
			case CREATE:
				return this.createViews;
			case DELETE:
				return this.deleteViews;
			case UPDATE:
				return this.updateViews;
		}
		return null;
	}
	
	public CMEObjectViewRelationship getViewByName(ObjectAction action, String viewName) {
		List<CMEObjectViewRelationship> views = this.getViews(action);
		for (CMEObjectViewRelationship view : views) {
			if (view.getViewName().equals(viewName)) {
				return view;
			}
		}
		return null;
	}

	public void addChildDef(CMEObjectChildDef childDef) {
		this.childDefs.add(childDef);
	}

	public List<CMEObjectChildDef> getChildDefs() {
		return Collections.unmodifiableList(this.childDefs);
	}
	
	public boolean hasChildDefs() {
		return this.childDefs.size() > 0;
	}
	
	/**
	 * Checks whether the given Set of Strings contains all key column names for this CMEObjectDef.
	 * @param columnNames Set of column names to check.
	 * @return true if columnNames contains all the key column names; false otherwise.
	 */
	public boolean containsAllKeyColumns(Set<String> columnNames) {
		return getKeyColumns()
			.stream()
			.map(columnDef -> columnNames.contains(columnDef.getColumnName()))
			.collect(Collectors.reducing(true, (result, containsKeyColumn) -> result && containsKeyColumn));
	}
}
