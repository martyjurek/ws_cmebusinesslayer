package com.misys.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.misys.enums.DataType;

public class DBSchemaInfo {
	private static Logger logger = LogManager.getLogger(DBSchemaInfo.class);
	static private Map<String, DBTable> tablesJDBCmap = new HashMap<String, DBTable>();
	
	public static DBTable getTable(String tableName) {
		if (DBSchemaInfo.tablesJDBCmap.get(tableName) == null) getTableInfo(tableName);
		DBTable table = DBSchemaInfo.tablesJDBCmap.get(tableName);
		if (table.getSize() == 0) {
			try {
				throw new Exception("Table " + tableName + " does not exist");
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		return table;
	}
	
	public static DBColumn getColumn(String tableName, String columnName) {
		DBTable table = DBSchemaInfo.getTable(tableName);
		return table.getColumn(columnName);
	}
	
	private static void getTableInfo(String tableName) {
		DBSchemaInfo.getTableInfo(tableName, null); 
	}
	
	private static void getTableInfo(String tableName, String dbPath) {
		logger.trace("Getting info for table " + tableName + "...");
		
		Connection connection = null;
	    try {
			connection = DataSourceManager.getConnection();

//			connection = DriverManager.getConnection("jdbc:sqlserver://TEXLCS6D2WN72;user=mon_auth;" +
//					"password=mon_auth;database=MonDev_3");  // For unit test TestDBSchemaInfo.java.

			DatabaseMetaData meta = connection.getMetaData();	      
			
			DBTable dbTable = new DBTable();
			dbTable.setTableName(tableName);
			addColumns(meta, tableName, dbTable);
			addPrimaryKeys(meta, tableName, dbTable);
			addImportedKeys(meta, tableName, dbTable);
			DBSchemaInfo.tablesJDBCmap.put(tableName, dbTable);
			
			connection.close();	
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }	
	}

	static private void addColumns(DatabaseMetaData meta, String tableName, DBTable dbTable) {
		try {
			ResultSet columnResult = meta.getColumns(null, "dbo", tableName, null);
			while (columnResult.next()) {	    	  
				String columnName = (columnResult.getString("COLUMN_NAME"));
				DBColumn dbColumn = new DBColumn();
				DBSchemaInfo.setColumnInfo(columnResult, dbColumn);
				if (dbColumn.isAutoIncrement()) {
					dbTable.setAutoIncrement(true);
				}
				dbTable.addColumn(columnName, dbColumn);
			}
			columnResult.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}	

	static private void setColumnInfo(ResultSet columnResult, DBColumn dbColumn) {
		try {
			String columnName = columnResult.getString("COLUMN_NAME");
			String columnTableName = columnResult.getString("TABLE_NAME");	    	 	    	  
			String dataTypeName = columnResult.getString("TYPE_NAME");	    	 	    	  
			//if (dataType.equals("int identity")) dataType = "int";
			
			DataType dataType;
			
			if (dataTypeName.indexOf("int") >= 0) {
				dataType = DataType.INTEGER;
			} else if (dataTypeName.equals("decimal")) {
				dataType = DataType.DECIMAL;
			} else {
				dataType = DataType.STRING;
			}

			Integer maxStringLength = null;
			Integer precision = null;
			Integer scale = null;		
			if (dataType.equals("decimal") ||
				dataType.equals("int")     ||
				dataType.equals("numeric") ||
				dataType.equals("float")) {
			
				precision = Integer.valueOf(columnResult.getString("COLUMN_SIZE"));
				scale = Integer.valueOf(columnResult.getString("DECIMAL_DIGITS"));
				  
			} else {
				maxStringLength = Integer.valueOf(columnResult.getString("COLUMN_SIZE"));
			}
			
			boolean isNullable = false;
			if (columnResult.getString("IS_NULLABLE").equals("YES")) isNullable = true; 	    		  
	
			boolean isAutoIncrement = false;
			if (columnResult.getString("IS_AUTOINCREMENT").equals("YES")) isAutoIncrement = true; 	    		  
	
			String defaultValue = columnResult.getString("COLUMN_DEF");	    
			Integer ordinalPosition = Integer.valueOf(columnResult.getString("ORDINAL_POSITION"));
			
			dbColumn.setColumnName(columnName);
			dbColumn.setColumnTableName(columnTableName);
			dbColumn.setDataType(dataType);
			dbColumn.setMaxStringLength(maxStringLength);
			dbColumn.setPrecision(precision);
			dbColumn.setScale(scale);
			dbColumn.setIsNullable(isNullable);
			dbColumn.setIsAutoIncrement(isAutoIncrement);
			dbColumn.setDefaultValue(defaultValue);
			dbColumn.setOrdinalPosition(ordinalPosition);		
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
		
	static private void addPrimaryKeys(DatabaseMetaData meta, String tableName, DBTable dbTable) {
		try {
			ResultSet columnResult = meta.getPrimaryKeys(null, "dbo", tableName);
			while (columnResult.next()) {
				String columnName = (columnResult.getString("COLUMN_NAME"));				
				dbTable.getColumn(columnName).setIsPrimaryKey(true);
			}			
			columnResult.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}			
	}
		
	static private void addImportedKeys(DatabaseMetaData meta, String tableName, DBTable dbTable) {
		try {
			ResultSet columnResult = meta.getImportedKeys(null, "dbo", tableName);
			while (columnResult.next()) {
				String columnName = (columnResult.getString("FKCOLUMN_NAME"));
				dbTable.getColumn(columnName).setIsForeignKey(true);
			}
			columnResult.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
}
