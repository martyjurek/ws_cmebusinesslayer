package com.misys.definitions;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.misys.cmeobject.search.SQLQueryBuilder;
import com.misys.cmeobject.search.Queries.Query;
import com.misys.enums.OperatorType;
import com.misys.enums.ViewType;
import com.misys.jdbc.DBColumn;
import com.misys.jdbc.DBSchemaInfo;
import com.misys.jdbc.DBTable;
import com.misys.jdbc.SQLInput;
import com.misys.jdbc.SQLObject;

public class CMEViewSQLBuilder {
	
	private static Map<Long, SQLObject> sqlObjects = new HashMap<Long, SQLObject>();

	private static long getKey(CMEViewDef view, List<CMEViewColumnDef> columns,
			List<CMEViewPredicateDef> predicates) {
		
		long key = view.hashCode();
		if (columns != null) {
			for (CMEViewColumnDef column : columns) {
				key += column.hashCode();
			}
		}
		if (predicates != null) {
			for (CMEViewPredicateDef predicate : predicates) {
				key += predicate.hashCode();
			}
		}
		return key;
	}
	
	public static SQLObject getSQLObjectFromView(CMEViewDef view, List<CMEViewColumnDef> columns,
			List<CMEViewPredicateDef> predicates) {
		
		long key = CMEViewSQLBuilder.getKey(view, columns, predicates);
		SQLObject sqlObject = CMEViewSQLBuilder.sqlObjects.get(key);
		if (sqlObject == null) {
			sqlObject = CMEViewSQLBuilder.buildSQLObject(view, columns, predicates);
			CMEViewSQLBuilder.sqlObjects.put(key, sqlObject);
		}
		return sqlObject;
	}
	
	public static SQLObject getSQLObjectFromQuery(CMEViewDef view, List<CMEViewColumnDef> columns, SQLQueryBuilder builder) {
	    SQLObject sqlObject = new SQLObject();
	    ViewType viewType = view.getViewType();
	    if (!ViewType.SELECT.equals(viewType)) throw new UnsupportedOperationException();
	    sqlObject.addSQL("SELECT ");
	    CMEViewSQLBuilder.processSelectColumnString(sqlObject, columns);
	    CMEViewSQLBuilder.processJoins(sqlObject, view);
	    StringBuilder joinBuilder = builder.getJoinBuilder();
	    if (joinBuilder.length() > 0) {
	        sqlObject.addSQL(" ");
	        sqlObject.addSQL(joinBuilder);
	    }	    
	    sqlObject.addSQL(" WHERE ");
	    sqlObject.addSQL(builder.getWhereBuilder());	    
	    return sqlObject;
	}
	
	private static SQLObject buildSQLObject(CMEViewDef view, List<CMEViewColumnDef> columns,
			List<CMEViewPredicateDef> predicates) {
		
		boolean usePredicates = false;
		boolean useAliases = false;
		SQLObject sqlObject = new SQLObject();
		ViewType viewType = view.getViewType();
		switch (viewType) {
			case SELECT:
				sqlObject.addSQL("SELECT ");
				CMEViewSQLBuilder.processSelectColumnString(sqlObject, columns);
				CMEViewSQLBuilder.processJoins(sqlObject, view);
				usePredicates = true;
				useAliases = true;
				break;
			case UPDATE:
				sqlObject.addSQL("UPDATE ");
				sqlObject.addSQL(CMEViewSQLBuilder.getMainTable(view).getTableName());
				sqlObject.addSQL(" SET ");
				CMEViewSQLBuilder.processUpdateColumnString(sqlObject, columns, predicates);
				usePredicates = true;
				break;
				/*
			case IDENTITY_INSERT:
				sqlObject.addSQL("INSERT INTO ");
				sqlObject.addSQL(CMEViewSQLBuilder.getMainTable(view).getTableName());
				sqlObject.addSQL(" ");
				CMEViewSQLBuilder.processIdentityInsertColumnString(sqlObject, columns, view.getKeyColumns());
				break;
			case INSERT:
				String tableName = CMEViewSQLBuilder.getMainTable(view).getTableName();
				sqlObject.addSQL("INSERT INTO ");
				sqlObject.addSQL(tableName);
				sqlObject.addSQL(" ");
				CMEViewSQLBuilder.processInsertColumnString(sqlObject, tableName, columns, view.getKeyColumns());
				break;
				*/
			case INSERT:
			case IDENTITY_INSERT:
				String tableName = CMEViewSQLBuilder.getMainTable(view).getTableName();
				sqlObject.addSQL("INSERT INTO ");
				sqlObject.addSQL(tableName);
				sqlObject.addSQL(" ");
				CMEViewSQLBuilder.processInsertString(sqlObject, tableName, columns);
				break;
			case DELETE:
				sqlObject.addSQL("DELETE FROM ");
				sqlObject.addSQL(CMEViewSQLBuilder.getMainTable(view).getTableName());
				usePredicates = true;
				break;
		}
		
		if (usePredicates && predicates.size() > 0) {
			sqlObject.addSQL(" WHERE ");
			CMEViewSQLBuilder.processPredicates(sqlObject, view.getColumns(), predicates, useAliases);
		}
		
		return sqlObject;
	}
	
	private static CMEViewTableDef getMainTable(CMEViewDef view) {
		if (view.getTables().size() > 0) {
			//Assume all first tables are main
			return view.getTables().get(0);
		}
		return null;
	}
	
	private static void processJoins(SQLObject sqlObject, CMEViewDef view) {
		ListIterator<CMEViewTableDef> tableIt = view.getTables().listIterator();
		while (tableIt.hasNext()) {
			//Assume first table in list is the main table
			boolean firstTable = !tableIt.hasPrevious();
			CMEViewTableDef table = tableIt.next();
			StringBuilder joinSb = new StringBuilder();
			if (firstTable) {
				sqlObject.addSQL(" FROM ");
			} else {
				ListIterator<CMEViewJoinDef> joinIt = view.getJoinsByAlias(table.getAlias()).listIterator();
				while (joinIt.hasNext()) {
					//Assume first join in list is the main join
					boolean firstJoin = !joinIt.hasPrevious();
					CMEViewJoinDef join = joinIt.next();
					if (firstJoin) {
						sqlObject.addSQL(" ");
						sqlObject.addSQL(join.getJoinType().toString());
						sqlObject.addSQL(" JOIN ");
					}
					joinSb.append(" ");
					joinSb.append(join.getTableAlias());
					joinSb.append(".");
					joinSb.append(join.getColumnName());
					joinSb.append(" = ");
					joinSb.append(join.getParentTableAlias());
					joinSb.append(".");
					joinSb.append(join.getParentColumnName());
				}
			}
			sqlObject.addSQL(table.getTableName());
			sqlObject.addSQL(" ");
			sqlObject.addSQL(table.getAlias());
			if (joinSb.length() > 0) {
				sqlObject.addSQL(" ON");
				sqlObject.addSQL(joinSb);
			}
		}
	}
	
	private static void processSelectColumnString(SQLObject sqlObject, List<CMEViewColumnDef> columns) {
		ListIterator<CMEViewColumnDef> columnIt = columns.listIterator();
		while (columnIt.hasNext()) {
			if (columnIt.hasPrevious()) {
				sqlObject.addSQL(", ");
			}
			CMEViewColumnDef column = columnIt.next();
			sqlObject.addSQL(column.getTableAlias());
			sqlObject.addSQL(".");
			sqlObject.addSQL(column.getColumnName());
		}
	}
	
	private static void processUpdateColumnString(SQLObject sqlObject, List<CMEViewColumnDef> columns, List<CMEViewPredicateDef> predicates) {
		ListIterator<CMEViewColumnDef> columnIt = columns.listIterator();
		StringBuilder sb = new StringBuilder();
		while (columnIt.hasNext()) {
			StringBuilder colSB = new StringBuilder();
			if (sb.length() > 0) {
				colSB.append(", ");
			}
			CMEViewColumnDef column = columnIt.next();
			colSB.append(column.getColumnName());
			colSB.append(" = ?");
			
			if (!CMEViewSQLBuilder.containsPredicateColumnName(column.getColumnName(), predicates)) {
				sb.append(colSB);
				DBColumn dbColumn = DBSchemaInfo.getColumn(column.getTableName(), column.getColumnName());
				sqlObject.addInput(new SQLInput(column.getColumnName(), dbColumn.getDataType()));
			}
		}
		sqlObject.addSQL(sb);
	}
	
	public static boolean containsPredicateColumnName(String columnName, List<CMEViewPredicateDef> predicates) {
		for (CMEViewPredicateDef predicate : predicates) {
			if (predicate.containsColumnName(columnName)) {
				return true;
			}
		}
		return false;
	}
	
	private static void processInsertString(SQLObject sqlObject, String tableName, List<CMEViewColumnDef> columns) {
		
		DBTable dbTable = DBSchemaInfo.getTable(tableName);
		if (dbTable.isAutoIncrement()) {
			CMEViewSQLBuilder.processIdentityInsertColumnString(sqlObject, tableName, columns);
		} else {
			CMEViewSQLBuilder.processInsertColumnString(sqlObject, tableName, columns);
		}
	}
	
	private static void processIdentityInsertColumnString(SQLObject sqlObject, String tableName, List<CMEViewColumnDef> columns) {
		
		StringBuilder columnSB = new StringBuilder();
		StringBuilder inputSB = new StringBuilder();
		StringBuilder outputSB = new StringBuilder();
		
		List<DBColumn> primaryKeys = DBSchemaInfo.getTable(tableName).getPrimaryNonForeignKeys();
		
		ListIterator<CMEViewColumnDef> columnIt = columns.listIterator();
		while (columnIt.hasNext()) {
			boolean previous = columnIt.hasPrevious();
			CMEViewColumnDef viewColumn = columnIt.next();
			DBColumn dbColumn = DBSchemaInfo.getColumn(tableName, viewColumn.getColumnName());
			if (!primaryKeys.contains(dbColumn)) {
				if (previous) {
					columnSB.append(", ");
					inputSB.append(", ");
					if (outputSB.length() > 0) {
						outputSB.append(", ");
					}
				}
				columnSB.append(viewColumn.getColumnName());
				inputSB.append("?");
				sqlObject.addInput(new SQLInput(viewColumn.getColumnName(), dbColumn.getDataType()));
			}
		}
		
		ListIterator<DBColumn> keyIt = primaryKeys.listIterator();
		if (keyIt.hasNext()) {
			while (keyIt.hasNext()) {
				boolean previous = keyIt.hasPrevious();
				DBColumn dbColumn = keyIt.next();
				if (previous) {
					outputSB.append(", ");
				} else {
					outputSB.append(" OUTPUT ");
				}
				outputSB.append("INSERTED.");
				outputSB.append(dbColumn.getColumnName());
			}
		} else {
			outputSB.append(" OUTPUT NULL ");
		}
		
		sqlObject.addSQL("(");
		sqlObject.addSQL(columnSB);
		sqlObject.addSQL(")");
		sqlObject.addSQL(outputSB);
		sqlObject.addSQL(" VALUES (");
		sqlObject.addSQL(inputSB);
		sqlObject.addSQL(")");
	}

	private static void processInsertColumnString(SQLObject sqlObject, String tableName, List<CMEViewColumnDef> columns) {

		StringBuilder sb = new StringBuilder();
		StringBuilder names = new StringBuilder();
		StringBuilder values = new StringBuilder();
		StringBuilder output = new StringBuilder();
		StringBuilder select = new StringBuilder();

		List<DBColumn> primaryKeys = DBSchemaInfo.getTable(tableName).getPrimaryNonForeignKeys();		
		
		if (primaryKeys.size() > 0) {
			ListIterator<DBColumn> keyIt = primaryKeys.listIterator();
			while (keyIt.hasNext()) {
				if (keyIt.hasPrevious()) {
					names.append(",");
					names.append(" ");
				}	
				DBColumn column = keyIt.next();
				String keyName = column.getColumnName();
				names.append(keyName);
				
				if (output.length() > 0) {
					output.append(",");
				}
				output.append(" ");
				output.append("INSERTED.");
				output.append(keyName);
				
				if (select.length() > 0) {
					select.append(",");
				}
				select.append("(COALESCE(MAX(");
				select.append(keyName);
				select.append("),0)+1)");
				select.append(" ");
				select.append("AS");
				select.append(" ");
				select.append(keyName);
			}
		} else {
			output.append(" INSERTED.*");
		}
		
		if (columns != null && columns.size() > 0) {
			ListIterator<CMEViewColumnDef> columnIt = columns.listIterator();
			while (columnIt.hasNext()) {
				CMEViewColumnDef viewColumn = columnIt.next();
				DBColumn dbColumn = DBSchemaInfo.getColumn(tableName, viewColumn.getColumnName());
				if (!primaryKeys.contains(dbColumn)) {
					String name = dbColumn.getColumnName();
					if (select.indexOf(dbColumn.getColumnName()) > 0) {
						continue;
					}
					if (names.length() > 0) {
						names.append(",");
						names.append(" ");
					}
					names.append(name);
					
					if (values.length() > 0) {
						values.append(",");
						values.append(" ");
					}
					
					if (select.length() > 0) {
						select.append(",");
						select.append(" ");
					}
					select.append("?");
					select.append(" ");
					select.append("AS");
					select.append(" ");
					select.append(name);
					sqlObject.addInput(new SQLInput(name, dbColumn.getDataType()));
				}
			}
		}

		sb.append(" ");
		sb.append("(");
		sb.append(names);
		sb.append(")");
		
		sb.append(" ");
		sb.append("OUTPUT");
		sb.append(output);
		sb.append(" ");
		
		sb.append("SELECT");
		if (primaryKeys.size() > 0) {
			sb.append(" TOP 1");
		}
		sb.append(" ");
		sb.append(select);
		sb.append(" ");
		sb.append("FROM");
		sb.append(" ");
		sb.append(tableName);
		
		sqlObject.addSQL(sb);
	}
	
	private static void processPredicates(SQLObject sqlObject, List<CMEViewColumnDef> columns,
			List<CMEViewPredicateDef> predicates, boolean useAliases) {
		ListIterator<CMEViewPredicateDef> predicateIt = predicates.listIterator();
		while (predicateIt.hasNext()) {
			if (predicateIt.hasPrevious()) {
				sqlObject.addSQL(" AND ");
			}
			CMEViewPredicateDef predicate = predicateIt.next();
			CMEViewSQLBuilder.processOperandString(sqlObject, columns, predicate.getAlias(),
					predicate.getLeftOperand(), useAliases);
			sqlObject.addSQL(" ");
			CMEViewSQLBuilder.processOperatorString(sqlObject, predicate.getOperatorType());
			sqlObject.addSQL(" ");
			CMEViewSQLBuilder.processOperandString(sqlObject, columns, predicate.getAlias(),
					predicate.getRightOperand(), useAliases);
		}
	}
	
	private static void processOperatorString(SQLObject sqlObject, OperatorType type) {
		switch (type) {
			case EQUALS:
				sqlObject.addSQL("=");
				break;
			case NOT_EQUALS:
				sqlObject.addSQL("!=");
				break;
			case LIKE:
				sqlObject.addSQL("LIKE");
				break;
			case NOT_LIKE:
				sqlObject.addSQL("NOT LIKE");
				break;
			case IN:
				sqlObject.addSQL("IN");
				break;
			case NOT_IN:
				sqlObject.addSQL("NOT IN");
				break;
			case GREATER_THAN:
				sqlObject.addSQL(">");
				break;
			case GREATER_THAN_EQUALS:
				sqlObject.addSQL(">=");
				break;
			case LESS_THAN:
				sqlObject.addSQL("<");
				break;
			case LESS_THAN_EQUALS:
				sqlObject.addSQL("<=");
				break;
		}
	}
	
	private static void processOperandString(SQLObject sqlObject, List<CMEViewColumnDef> columns, 
			String alias, CMEViewOperandDef operand, boolean useAliases) {
		switch (operand.getOperandType()) {
			case COLUMN:
				if (useAliases) {
					sqlObject.addSQL(alias + "." + operand.getColumnName());
				} else {
					sqlObject.addSQL(operand.getColumnName());
				}
				break;
			case INPUT:
				sqlObject.addSQL("?");
				for (CMEViewColumnDef column : columns) {
					if (column.getColumnName().equals(operand.getColumnName())) {
						sqlObject.addInput(new SQLInput(operand.getColumnName(), operand.getDataType()));
						break;
					}
				}
				break;
			case LITERAL_STRING:
				sqlObject.addSQL(operand.getLiteralString());
				break;
		}
	}
	
}