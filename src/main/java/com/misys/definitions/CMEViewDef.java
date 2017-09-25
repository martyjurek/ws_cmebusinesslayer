package com.misys.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.misys.enums.ObjectAction;
import com.misys.enums.ViewType;
import com.misys.jdbc.DBColumn;
import com.misys.jdbc.DBSchemaInfo;

public class CMEViewDef {

	private static Map<String, CMEViewDef> viewDefs = new HashMap<String, CMEViewDef>();
	
	public static void addViewDef(CMEViewDef viewDef) {
		CMEViewDef.viewDefs.put(viewDef.getViewName(), viewDef);
	}
	
	public static CMEViewDef getViewDef(String name) {
		return CMEViewDef.viewDefs.get(name);
	}
	
	private String viewName;
	private ViewType viewType;
	private List<CMEViewTableDef> tables;
	private List<CMEViewColumnDef> columns;
	private List<CMEViewJoinDef> joins;
	private List<CMEViewPredicateDef> predicates;
	
	public CMEViewDef(String viewName, ViewType viewType) {
		this.viewName = viewName;
		this.viewType = viewType;
		this.tables = new ArrayList<CMEViewTableDef>();
		this.columns = new ArrayList<CMEViewColumnDef>();
		this.joins = new ArrayList<CMEViewJoinDef>();
		this.predicates = new ArrayList<CMEViewPredicateDef>();
	}

	public String getViewName() {
		return this.viewName;
	}

	public ViewType getViewType() {
		return this.viewType;
	}
	
	public void addTable(CMEViewTableDef table) {
		this.tables.add(table);
	}
	
	public CMEViewTableDef getTableByAlias(String alias) {
		for (CMEViewTableDef table : this.tables) {
			if (table.getAlias().equalsIgnoreCase(alias)) {
				return table;
			}
		}
		return null;
	}
	
	public List<CMEViewTableDef> getTables() {
		return Collections.unmodifiableList(this.tables);
	}
	
	public void addColumn(CMEViewColumnDef column) {
		this.columns.add(column);
	}

	public List<CMEViewColumnDef> getColumns() {
		return Collections.unmodifiableList(this.columns);
	}
	
	public CMEViewColumnDef getColumnByName(String name) {
		for (CMEViewColumnDef column : this.columns) {
			if (column.getColumnName().equalsIgnoreCase(name)) {
				return column;
			}
		}
		return null;
	}
	
	public List<CMEViewColumnDef> getKeyColumns() {
		List<CMEViewColumnDef> keyColumns = new ArrayList<CMEViewColumnDef>();
		for (CMEViewColumnDef column : this.columns) {			
			CMEViewTableDef table = this.getTableByAlias(column.getTableAlias());
			DBColumn dbColumn = DBSchemaInfo.getColumn(table.getTableName(), column.getColumnName());
			if (dbColumn.isPrimaryKey()) {
				keyColumns.add(column);
			}
		}
		return keyColumns;
	}
	
	public List<CMEViewColumnDef> getColumnsByNames(List<String> names) {
		List<CMEViewColumnDef> tableColumns = new ArrayList<CMEViewColumnDef>();
		for (String name : names) {
			CMEViewColumnDef column = this.getColumnByName(name);
			if (column != null) {
				tableColumns.add(column);
			}
		}
		return tableColumns;
	}
	
	public List<CMEViewColumnDef> getColumnsByAlias(String alias) {
		List<CMEViewColumnDef> tableColumns = new ArrayList<CMEViewColumnDef>();
		for (CMEViewColumnDef column : this.columns) {
			if (column.getTableAlias().equalsIgnoreCase(alias)) {
				tableColumns.add(column);
			}
		}
		return tableColumns;
	}
	
	public void addJoin(CMEViewJoinDef join) {
		this.joins.add(join);
	}

	public List<CMEViewJoinDef> getJoins() {
		return Collections.unmodifiableList(this.joins);
	}
	
	public List<CMEViewJoinDef> getJoinsByAlias(String alias) {
		List<CMEViewJoinDef> tableJoins = new ArrayList<CMEViewJoinDef>();
		for (CMEViewJoinDef join : this.joins) {
			if (join.getTableAlias().equalsIgnoreCase(alias)) {
				tableJoins.add(join);
			}
		}
		return tableJoins;
	}
	
	public void addPredicate(CMEViewPredicateDef predicate) {
		this.predicates.add(predicate);
	}

	public List<CMEViewPredicateDef> getPredicates() {
		return Collections.unmodifiableList(this.predicates);
	}
	
	public List<String> getPredicateColumnNames() {
		List<String> list = new ArrayList<String>();
		for (CMEViewPredicateDef predicate : this.predicates) {
			String leftColName = predicate.getLeftOperand().getColumnName();
			if (leftColName != null) list.add(leftColName);
			String rightColName = predicate.getRightOperand().getColumnName();
			if (rightColName != null) list.add(rightColName);
			list.add(predicate.getRightOperand().getColumnName());
		}
		return list;
	}
	
	public List<CMEViewPredicateDef> getPredicatesByAlias(String alias) {
		List<CMEViewPredicateDef> tablePredicate = new ArrayList<CMEViewPredicateDef>();
		for (CMEViewPredicateDef predicate : this.predicates) {
			if (predicate.getAlias().equalsIgnoreCase(alias)) {
				tablePredicate.add(predicate);
			}
		}
		return tablePredicate;
	}
	
	public boolean containsPredicateColumnName(String columnName) {
		for (CMEViewPredicateDef predicate : this.predicates) {
			if (predicate.containsColumnName(columnName)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.viewName.hashCode() * this.viewType.hashCode();
	}

}
