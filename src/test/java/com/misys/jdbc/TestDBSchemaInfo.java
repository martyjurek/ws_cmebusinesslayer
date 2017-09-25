package com.misys.jdbc;

import java.util.List;
import com.misys.jdbc.DBSchemaInfo;

public class TestDBSchemaInfo {

	public static void main(String [] args) {

		// Test 1
		DBColumn column = DBSchemaInfo.getTable("t_ccs_ent").getColumn("ent_id");
		String expectedOutput = 
				"\nCOLUMN_NAME: ent_id\n" +
				"   TABLE_NAME: t_ccs_ent\n" +
				"   DATA_TYPE: int\n" +
				"   MAX_STRING_LENGTH: null\n" +
				"   PRECISION: 10\n" +
				"   SCALE: 0\n" +
				"   IS_NULLABLE: false\n" +
				"   COLUMN_DEFAULT: null\n" +
				"   IS_AUTOINCREMENT: true\n" +
				"   IS_PRIMARY_KEY: true\n" +
				"   IS_FOREIGN_KEY: false\n" +
				"   ORDINAL_POSITION: 1";
		String actualOutput = column.getColumnInfo();	
//		System.out.println(actualOutput);	
		boolean testResult = expectedOutput.equals(actualOutput);
		System.out.println("\nTest 1: Test Passed? " + testResult);	
		System.out.println("\n===============");

		
		// Test 2
		column = DBSchemaInfo.getTable("t_ccs_loan").getColumn("ln_obgat_num");
		expectedOutput = 
				"\nCOLUMN_NAME: ln_obgat_num\n" +
				"   TABLE_NAME: t_ccs_loan\n" +
				"   DATA_TYPE: nvarchar\n" +
				"   MAX_STRING_LENGTH: 26\n" +
				"   PRECISION: null\n" +
				"   SCALE: null\n" +
				"   IS_NULLABLE: true\n" +
				"   COLUMN_DEFAULT: null\n" +
				"   IS_AUTOINCREMENT: false\n" +
				"   IS_PRIMARY_KEY: false\n" +
				"   IS_FOREIGN_KEY: false\n" +
				"   ORDINAL_POSITION: 9";		
		actualOutput = column.getColumnInfo();
//		System.out.println(actualOutput);	
		testResult = expectedOutput.equals(actualOutput);
		System.out.println("\nTest 2: Test Passed? " + testResult);	
		System.out.println("\n===============");

		
		// Test 3
		String tableName = "t_ccs_ent_obligor_num_rel";
		List<DBColumn> primaryKeys = DBSchemaInfo.getTable(tableName).getPrimaryKeys();
		expectedOutput = "ent_bsys_id,ent_id,ent_bank_code,";
		actualOutput = "";
		for (DBColumn primaryKey : primaryKeys) {
			actualOutput += primaryKey.getColumnName() + ",";			
		}
//		System.out.println(actualOutput);	
		testResult = expectedOutput.equals(actualOutput);
		System.out.println("\nTest 3: Test Passed? " + testResult);	
		System.out.println("\n===============");
		

		// Test 4
		tableName = "t_ccs_ent_obligor_num_rel";
		List<DBColumn> foreignKeys = DBSchemaInfo.getTable(tableName).getForeignKeys();
		expectedOutput = "ent_id,";
		actualOutput = "";
		for (DBColumn foreignKey : foreignKeys) {
			actualOutput += foreignKey.getColumnName() + ",";			
		}
//		System.out.println(actualOutput);	
		testResult = expectedOutput.equals(actualOutput);
		System.out.println("\nTest 4: Test Passed? " + testResult);	
		System.out.println("\n===============");
	}
}
