package com.misys.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DBTable {

	private Map<String, DBColumn> columnsMap;		
	private String tableName;
	private boolean autoIncrement;

	public DBTable() {
		this.columnsMap = new HashMap<String, DBColumn>();		
	}
	
	public DBColumn getColumn(String columnName) {
		DBColumn column = this.columnsMap.get(columnName);
		if (column == null) {
			try {
				throw new Exception("Column " + columnName + " does not exist");
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		return column;
	}

	public String getTableName() {
		return this.tableName;
	}

	public int getSize() {
		return this.columnsMap.size();
	}

	public List<DBColumn> getPrimaryKeys() {
		List<DBColumn> primaryKeysList = new ArrayList<DBColumn>();
		Iterator<Entry<String, DBColumn>> it = this.columnsMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Entry<String, DBColumn> pair = it.next();
	        DBColumn dbColumn = pair.getValue();
	        if (dbColumn.isPrimaryKey()) {
	        	primaryKeysList.add(dbColumn);
	        }
	    }
		return Collections.unmodifiableList(primaryKeysList);
	}
	
	public List<DBColumn> getPrimaryNonForeignKeys() {
		List<DBColumn> primaryKeysList = new ArrayList<DBColumn>();
		Iterator<Entry<String, DBColumn>> it = this.columnsMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Entry<String, DBColumn> pair = it.next();
	        DBColumn dbColumn = pair.getValue();
	        if (dbColumn.isPrimaryKey() && !dbColumn.isForeignKey()) {
	        	primaryKeysList.add(dbColumn);
	        }
	    }
		return Collections.unmodifiableList(primaryKeysList);
	}	
	
	public List<DBColumn> getForeignKeys() {
		List<DBColumn> foreignKeysList = new ArrayList<DBColumn>();
		Iterator<Entry<String, DBColumn>> it = this.columnsMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Entry<String, DBColumn> pair = it.next();
	        DBColumn dbColumn = pair.getValue();
	        if (dbColumn.isForeignKey()) {
	        	foreignKeysList.add(dbColumn);
	        }
	    }
		return Collections.unmodifiableList(foreignKeysList);
	}

	public String getTableInfo() {
		Iterator<Entry<String, DBColumn>> it = this.columnsMap.entrySet().iterator();
		String tableInfo = "";
	    while (it.hasNext()) {
	        Entry<String, DBColumn> pair = it.next();
	        DBColumn columnInfo = pair.getValue();
	        tableInfo += columnInfo.getColumnInfo() + '\n';
	    }
	    return tableInfo;
	}

	protected void addColumn(String columnName, DBColumn columnInfo) {
		this.columnsMap.put(columnName, columnInfo);
	}
	
	protected void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	protected void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}
	
	public boolean isAutoIncrement() {
		return this.autoIncrement;
	}
}
