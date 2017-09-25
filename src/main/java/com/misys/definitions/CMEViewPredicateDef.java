package com.misys.definitions;

import com.misys.enums.OperatorType;

public class CMEViewPredicateDef {

	private String alias;
	private OperatorType operatorType;
	private CMEViewOperandDef leftOperand;
	private CMEViewOperandDef rightOperand;
	
	public CMEViewPredicateDef(String alias, OperatorType operatorType, 
			CMEViewOperandDef leftOperand, CMEViewOperandDef rightOperand) {
		
		this.alias = alias;
		this.operatorType = operatorType;
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}

	public String getAlias() {
		return this.alias;
	}
	
	public OperatorType getOperatorType() {
		return this.operatorType;
	}

	public CMEViewOperandDef getLeftOperand() {
		return this.leftOperand;
	}

	public CMEViewOperandDef getRightOperand() {
		return this.rightOperand;
	}
	
	public boolean containsColumnName(String columnName) {
		if ((this.leftOperand.getColumnName() != null && this.leftOperand.getColumnName().equals(columnName))
				|| (this.rightOperand.getColumnName() != null && this.rightOperand.getColumnName().equals(columnName))) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.alias.hashCode() * this.operatorType.hashCode() * 
				this.leftOperand.hashCode() * this.rightOperand.hashCode();
	}

}
