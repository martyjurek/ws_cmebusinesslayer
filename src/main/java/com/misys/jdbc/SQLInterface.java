package com.misys.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.misys.cmeobject.search.Queries.ValueNode;

//import net.sourceforge.jtds.jdbcx.JtdsDataSource;

public class SQLInterface {
	
	/*
	private static JtdsDataSource dataSource;
	
	//Temporary testing
	static {
		try {
			System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
	        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
			
			InitialContext ic = new InitialContext();
	        ic.createSubcontext("java:");
	        ic.createSubcontext("java:/comp");
	        ic.createSubcontext("java:/comp/env");
	        ic.createSubcontext("java:/comp/env/jdbc");        
	        
	        SQLInterface.dataSource = new JtdsDataSource();
	        SQLInterface.dataSource.setServerType(1);
	        SQLInterface.dataSource.setServerName("TEXLDEFHK4HC2");
	        SQLInterface.dataSource.setInstance("MSSQLSERVER2012");
	        SQLInterface.dataSource.setDatabaseName("MonDev");
	        SQLInterface.dataSource.setUser("mon_auth");
	        SQLInterface.dataSource.setPassword("mon_auth");	      
	        
	        ic.bind("CF_SYS_DS", SQLInterface.dataSource);

	        Class dsClass = Class.forName("net.sourceforge.jtds.jdbc.Driver");
	        
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static Connection getConnection() {
		Connection connection = null;
		try {
			connection = DataSourceManager.getConnection();
			if (connection.getAutoCommit()) {
				connection.setAutoCommit(false);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return connection;
	}
	*/
	
	public static Map<String, Object> executeQuerySingleRow(SQLObject sqlObject) {
		return SQLInterface.executeQuerySingleRow(sqlObject, null);
	}
	
	public static Map<String, Object> executeQuerySingleRow(SQLObject sqlObject, Map<String, Object> parameters) {
		List<Map<String, Object>> results = SQLInterface.executeQuery(sqlObject, parameters);
		if (results != null && results.size() > 0) {
			return results.get(0);
		}
		return new HashMap<String, Object>();
	}
	
	public static List<Map<String, Object>> executeQuery(SQLObject sqlObject) {
		return SQLInterface.executeQuery(sqlObject, (Map<String, Object>)null);
	}
	
	public static List<Map<String, Object>> executeQuery(SQLObject sqlObject, Map<String, Object> parameters) {
		List<Map<String, Object>> results;
		Connection connection = null;
		try {
			connection = DataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(sqlObject.getSQL());
			SQLInterface.processParameters(ps, sqlObject, parameters);
			ResultSet rs = ps.executeQuery();
			results = processResultSet(rs);
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (Exception c) {
				throw new RuntimeException(c);
			}
			throw new RuntimeException(e);
		} finally {
			try {
				connection.close();
			} catch (Exception c) {
				throw new RuntimeException(c);
			}
		}
		return results;
	}
	
	public static List<Map<String, Object>> executeQuery(SQLObject sqlObject, List<ValueNode> queryParameters) {
	    List<Map<String, Object>> results;
	    Connection connection = null;
	    try {
	        connection = DataSourceManager.getConnection();
	        PreparedStatement ps = connection.prepareStatement(sqlObject.getSQL());
	        SQLInterface.processQueryParameters(ps, queryParameters);
	        ResultSet rs = ps.executeQuery();
	        results = processResultSet(rs);
	        connection.commit();
	    } catch (SQLException e) {
	        try {
	            connection.rollback();
	        } catch (SQLException c) {
	            throw new RuntimeException(c);
	        }
	        throw new RuntimeException(e);
	    } finally {
	        try {
	            connection.close();
	        } catch (SQLException e) {
	            throw new RuntimeException(e);
	        }
	    }
	    return results;
	}
	
	public static Boolean execute(SQLObject sqlObject) {
		return SQLInterface.execute(sqlObject, null);
	}
	
	public static Boolean execute(SQLObject sqlObject, Map<String, Object> parameters) {
		boolean success = false;
		Connection connection = null;
		try {
			connection = DataSourceManager.getConnection();
			PreparedStatement ps = connection.prepareStatement(sqlObject.getSQL());
			SQLInterface.processParameters(ps, sqlObject, parameters);
			ps.execute();
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (Exception c) {
				throw new RuntimeException(c);
			}
			throw new RuntimeException(e);
		} finally {
			try {
				connection.close();
			} catch (Exception c) {
				throw new RuntimeException(c);
			}
		}
		return success;
	}
	
	private static void processParameters(PreparedStatement ps, SQLObject sqlObject, Map<String, Object> parameters) {
		try {
			if (sqlObject.getInputs().size() > 0 && parameters != null && parameters.size() > 0) {
				ListIterator<SQLInput> sqlInputIt = sqlObject.getInputs().listIterator();
				while (sqlInputIt.hasNext()) {
					SQLInput input = sqlInputIt.next();
					Object value = parameters.get(input.getName());
					//System.out.println(input.getName() + ", " + value + ", " + input.getType());
					switch (input.getType()) {
						case STRING:
							if (value != null && value instanceof Number) {
								ps.setString(sqlInputIt.nextIndex(), ((Number) value).toString());
							} else {
								ps.setString(sqlInputIt.nextIndex(), (String) value);
							}
							break;
						case INTEGER:
							if (value != null) {
								ps.setInt(sqlInputIt.nextIndex(), Integer.parseInt(value.toString()));
							} else {
								ps.setInt(sqlInputIt.nextIndex(), (Integer) value);
							}
							break;
						case DECIMAL:
							if (value != null) {
								ps.setDouble(sqlInputIt.nextIndex(), Double.parseDouble(value.toString()));
							} else {
								ps.setDouble(sqlInputIt.nextIndex(), (Double) value);
							}
							break;
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static List<Map<String, Object>> processResultSet(ResultSet rs) throws SQLException {
	    List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 1; i < rsmd.getColumnCount()+1; i++) {
                String name = rsmd.getColumnName(i);
                String type = rsmd.getColumnTypeName(i);
                if (type.equals("int")) {
                    map.put(name, rs.getInt(i));
                } else {
                    map.put(name, rs.getString(i));
                }
            }
            results.add(map);
        }
        return results;
	}
	
	private static void processQueryParameters(PreparedStatement ps, List<ValueNode> queryParameters) throws SQLException {
        for (int idx = 0; idx < queryParameters.size(); idx++) {
            int paramNum = idx + 1;
            ValueNode parameter = queryParameters.get(idx);
            if (parameter.isNull()) {
                ps.setNull(idx, Types.VARCHAR);
            } else {
	            switch (parameter.getValueType()) {
    	            case BOOLEAN:
    	                ps.setBoolean(paramNum, parameter.getBoolean());
    	                break;
    	            case DATE:
    	                ps.setDate(paramNum,  parameter.getDate());
    	                break;
    	            case NUMBER:
    	                Number number = parameter.getNumber();
    	                processNumber(ps, paramNum, number);
    	                break;
    	            case TEXT:
    	                ps.setString(paramNum, parameter.getText());
    	                break;
    	            case TIME:
    	                ps.setTime(paramNum, parameter.getTime());
    	                break;
    	            case TIMESTAMP:
    	                ps.setTimestamp(paramNum, parameter.getTimestamp());
    	                break;
    	            case UUID:
    	                ps.setString(paramNum, parameter.getUUID().toString());
    	                break;
    	            default:
    	                throw new UnsupportedOperationException();
	            }
            }
	    }
	}
	
	private static void processNumber(PreparedStatement ps, int idx, Number number) throws SQLException {
	    if (number instanceof Integer) {
            ps.setInt(idx, (Integer)number);
        } else if (number instanceof Long) {
            ps.setLong(idx, (Long)number);
        } else if (number instanceof Float) {
            ps.setFloat(idx, (Float)number);
        } else if (number instanceof Double) {
            ps.setDouble(idx, (Double)number);
        } else if (number instanceof BigDecimal) {
            ps.setBigDecimal(idx, (BigDecimal)number);
        } else {
            throw new UnsupportedOperationException();
        }
	}
}
