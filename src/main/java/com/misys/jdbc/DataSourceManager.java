package com.misys.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.sql.DataSource;

//import net.sourceforge.jtds.jdbcx.JtdsDataSource;

public class DataSourceManager {

	private static DataSource dataSource;
	
	public static void setDataSource(DataSource dataSource) {
		DataSourceManager.dataSource = dataSource;
	}
	
	public static Connection getConnection() throws SQLException {
		Connection connection = DataSourceManager.dataSource.getConnection();
		if (connection.getAutoCommit()) {
			connection.setAutoCommit(false);
		}
		return connection;
	}
	
}