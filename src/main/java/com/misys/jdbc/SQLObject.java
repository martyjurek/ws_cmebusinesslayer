package com.misys.jdbc;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple wrapper containing parameterized SQL and its input variables
 */
public class SQLObject {
    private StringBuilder sql;
    private List<SQLInput> inputs;

    public SQLObject() {
        this.sql = new StringBuilder();
        this.inputs = new ArrayList<SQLInput>();
    }

    public void addSQL(String sql) {
        this.sql.append(sql);
    }

    public void addSQL(StringBuilder sql) {
        this.sql.append(sql);
    }

    public void addInput(SQLInput input) {
        this.inputs.add(input);
    }

    public void append(SQLObject sqlObject) {
        sql.append(sqlObject.getSql());
        this.inputs.addAll(sqlObject.getInputs());
    }

    public StringBuilder getSql() {
        return this.sql;
    }

    public String getSQL() {
        return this.sql.toString();
    }

    public List<SQLInput> getInputs() {
        return this.inputs;
    }
}
