package com.misys.jdbc;

import com.misys.enums.DataType;

public class SQLInput {

	private String name;
	private DataType type;
	
	public SQLInput(String name, DataType type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return this.name;
	}

	public DataType getType() {
		return this.type;
	}
	
}
