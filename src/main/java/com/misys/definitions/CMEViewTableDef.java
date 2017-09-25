package com.misys.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CMEViewTableDef {

	private String tableName;
	private String alias;
	
	public CMEViewTableDef(String tableName, String alias) {
		this.tableName = tableName;
		this.alias = alias;
	}

	public String getTableName() {
		return this.tableName;
	}

	public String getAlias() {
		return this.alias;
	}

}
