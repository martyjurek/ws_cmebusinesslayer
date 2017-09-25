package com.misys.definitions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.misys.enums.CMEObjectType;
import com.misys.enums.DataType;
import com.misys.enums.JoinType;
import com.misys.enums.ObjectAction;
import com.misys.enums.OperandType;
import com.misys.enums.OperatorType;
import com.misys.enums.ViewType;
import com.misys.jdbc.SQLInput;
import com.misys.jdbc.SQLInterface;
import com.misys.jdbc.SQLObject;

/**
 * Contains static functions for loading various definitions.
 */
public class DefinitionLoader {
	/**
	 * Loads all definitions.
	 * The definitions will be loaded into the application's global state.
	 */
	public static void initialzeDefinitions() {
		DefinitionLoader.loadCMEObjectDefs();
		DefinitionLoader.loadCMEViewDefs();
		DefinitionLoader.loadCMEObjectViewAssociations();
		DefinitionLoader.loadCMEObjectChildAssociations();
	}
	
	/**
	 * Loads the CMEObject definitions from the database.
	 * @note The definitions are saved to CMEObjectDef.cmeObjects.
	 * TODO: Maybe make this function part of CMEObjectDef class?
	 */
	private static void loadCMEObjectDefs() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT co.cmeobj_id AS OBJECT_ID, ");
		sb.append("co.cmeobj_name AS OBJECT_NAME, ");
		sb.append("co.cmeobj_path AS API_PATH ");
		sb.append("FROM t_ccs_admin_cme_object co");

		SQLObject sqlObject = new SQLObject();
		sqlObject.addSQL(sb.toString());
		
		List<Map<String, Object>> objects = SQLInterface.executeQuery(sqlObject);
		for (Map<String, Object> map : objects) {
			int cmeObjId = Integer.parseInt(map.get("OBJECT_ID").toString());
			String cmeObjName = (String) map.get("OBJECT_NAME");
			String cmeObjPath = (String) map.get("API_PATH");
			CMEObjectDef def = new CMEObjectDef(cmeObjName, cmeObjPath);
			
			DefinitionLoader.loadCMEObjectColumnDefs(cmeObjId, def);
			
			CMEObjectDef.addCmeObjDef(def);
		}
	}
	
	/**
	 * Loads the column definitions for the given CMEObject ID into the given CMEObjectDef.
	 * @param cmeObjId ID of the CMEObject to load the column definitions for.
	 * @param def CMEObjectDef to load the column definitions into.
	 * TODO: Maybe make the CMEObjectDef constructor call this?
	 */
	private static void loadCMEObjectColumnDefs(final int cmeObjId, CMEObjectDef def) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT c.cocol_name AS COLUMN_NAME, "); 
		sb.append("vdt.code_val AS DATA_TYPE_CODE, ");
		sb.append("c.required_yn AS REQUIRED_YN, ");
		sb.append("c.key_yn AS KEY_YN, ");
		sb.append("c.visible_yn AS VISIBLE_YN, ");
		sb.append("c.default_value AS DEFAULT_VALUE ");
		//sb.append("rco.cmeobj_name AS REF_CME_OBJECT_NAME, ");
		//sb.append("rc.cocol_name AS REF_CME_COLUMN_NAME ");
		sb.append("FROM t_ccs_admin_cmeobj_column c ");
		//sb.append("JOIN t_ccs_admin_view_data_type vdt ON vdt.data_type_id = c.data_type_id ");
		sb.append("JOIN t_ccs_admin_code_sys vdt ON vdt.code_id = c.data_type_id ");
		//sb.append("LEFT JOIN t_ccs_admin_cme_object rco ON rco.cmeobj_id = c.ref_cmeobj_id ");
		//sb.append("LEFT JOIN t_ccs_admin_cmeobj_column rc ON rc.cocol_id = c.ref_cocol_id ");
		sb.append("WHERE c.cmeobj_id = ? ");
		sb.append("ORDER BY COLUMN_NAME");

		SQLObject sqlObject = new SQLObject();
		sqlObject.addSQL(sb.toString());
		sqlObject.addInput(new SQLInput("cmeObjId", DataType.INTEGER));
		
		Map<String, Object> queryParameters = new HashMap<>();
		queryParameters.put("cmeObjId", cmeObjId);
		List<Map<String, Object>> columns = SQLInterface.executeQuery(sqlObject, queryParameters);
		
		for (Map<String, Object> map : columns) {
			String columnName = (String) map.get("COLUMN_NAME");
			DataType dataType = DataType.getJoinTypeByCode((String) map.get("DATA_TYPE_CODE"));
			//System.out.println(columnName + ": " + map.get("DATA_TYPE_CODE") + ", " + dataType);
			boolean key = map.get("KEY_YN").equals("Y");
			boolean required = map.get("REQUIRED_YN").equals("Y");
			boolean visible = map.get("VISIBLE_YN").equals("Y");
			String defaultValue = (String) map.get("DEFAULT_VALUE");
			//String refObjectName = (String) map.get("REF_CME_OBJECT_NAME");
			//String refColumnName = (String) map.get("REF_CME_COLUMN_NAME");
			CMEObjectColumnDef column = new CMEObjectColumnDef(columnName, dataType, key, required,
					visible, defaultValue, null, null);
			def.addColumn(column);
		}
	}
	
	/**
	 * Loads the CME view definitions from the database.
	 */
	private static void loadCMEViewDefs() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT v.view_id AS VIEW_ID, ");
		sb.append("v.view_name AS VIEW_NAME, ");
		sb.append("vt.code_desc AS VIEW_TYPE_NAME, ");
		sb.append("vt.code_val AS VIEW_TYPE_CODE ");
		sb.append("FROM t_ccs_admin_view v ");
		//sb.append("JOIN t_ccs_admin_view_type vt ON vt.view_type_id = v.view_type_id");
		sb.append("JOIN t_ccs_admin_code_sys vt ON vt.code_id = v.view_type_id");

		SQLObject sqlObject = new SQLObject();
		sqlObject.addSQL(sb.toString());
		
		List<Map<String, Object>> views = SQLInterface.executeQuery(sqlObject);
		for (Map<String, Object> map : views) {
			int viewId = Integer.parseInt((String) map.get("VIEW_ID").toString().toString());
			String viewName = (String) map.get("VIEW_NAME");
			ViewType viewType = ViewType.getViewTypeByCode((String) map.get("VIEW_TYPE_CODE"));
			
			CMEViewDef viewDef = new CMEViewDef(viewName, viewType);

			DefinitionLoader.loadCMEViewTableDefs(viewId, viewDef);
			DefinitionLoader.loadCMEViewColumnDefs(viewId, viewDef);
			DefinitionLoader.loadCMEViewColumnJoins(viewId, viewDef);
			DefinitionLoader.loadCMEViewColumnPredicates(viewId, viewDef);
			
			CMEViewDef.addViewDef(viewDef);
		}
	}
	
	/**
	 * Loads the table definitions for the given CME view ID into the given CMEViewDef.
	 * @param viewId ID of the CME view to load the table definitions for.
	 * @param viewDef CMEViewDef to load the table definitions into.
	 * TODO: Remove VIEWTABLE_ID, TABLE_ID, and SEQUENCE from SELECT list?
	 * TODO: Maybe make the CMEViewDef constructor call this?
	 */
	private static void loadCMEViewTableDefs(final int viewId, CMEViewDef viewDef) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT vs.viewstbl_id AS VIEWTABLE_ID, ");
		sb.append("st.stbl_id AS TABLE_ID, ");
		sb.append("st.stbl_name AS TABLE_NAME, ");
		sb.append("vs.alias AS TABLE_ALIAS, ");
		sb.append("vs.sequence AS SEQUENCE ");
		sb.append("FROM t_ccs_admin_view_stbl vs ");
		sb.append("JOIN t_ccs_admin_sys_tbl st ON st.stbl_id = vs.stbl_id ");
		sb.append("WHERE vs.view_id = ? ");
		sb.append("ORDER BY vs.sequence");
		
		SQLObject sqlObject = new SQLObject();
		sqlObject.addSQL(sb.toString());
		sqlObject.addInput(new SQLInput("viewId", DataType.INTEGER));
		//System.out.println("viewId: " + viewId);
		Map<String, Object> queryParameters = new HashMap<>();
		queryParameters.put("viewId", viewId);
		List<Map<String, Object>> tables = SQLInterface.executeQuery(sqlObject, queryParameters);
		
		for (Map<String, Object> map : tables) {
			//int viewTableId = Integer.parseInt((String) map.get("VIEWTABLE_ID").toString());
			String tableName = (String) map.get("TABLE_NAME");
			String alias = (String) map.get("TABLE_ALIAS");
			
			CMEViewTableDef viewTable = new CMEViewTableDef(tableName, alias);
			viewDef.addTable(viewTable);
		}
	}
	
	/**
	 * Loads the column definitions for all tables that are part of the CME view with the given ID into the
	 * given CMEViewDef.
	 * @param viewId ID of the CME view to load the column definitions for.
	 * @param viewDef CMEViewDef to load the column definitions into.
	 * TODO: Maybe make the CMEViewDef constructor call this?
	 * TODO: Remove DATA_TYPE_CODE and KEY_YN from SELECT list?
	 */
	private static void loadCMEViewColumnDefs(final int viewId, CMEViewDef viewDef) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT stc.stcol_name AS COLUMN_NAME, ");
		sb.append("cs.code_desc AS DATA_TYPE_DESC, ");
		sb.append("cs.code_val AS DATA_TYPE_CODE, ");
		sb.append("stc.stcol_pk_yn AS KEY_YN, ");
		sb.append("st.stbl_name AS TABLE_NAME, ");
		sb.append("vst.alias AS TABLE_ALIAS ");
		sb.append("FROM t_ccs_admin_view_stcol vs ");
		sb.append("JOIN t_ccs_admin_view_stbl vst ON vst.viewstbl_id = vs.viewstbl_id ");
		sb.append("JOIN t_ccs_admin_sys_tbl_col stc ON stc.stcol_id = vs.stcol_id ");
		sb.append("JOIN t_ccs_admin_sys_tbl st ON st.stbl_id = stc.stbl_id ");
		sb.append("LEFT JOIN t_ccs_admin_code_sys cs ON cs.code_id = stc.stcol_type_id ");
		sb.append("WHERE vs.view_id = ? ");
		sb.append(" ORDER BY COLUMN_NAME");
		
		SQLObject sqlObject = new SQLObject();
		sqlObject.addSQL(sb.toString());
		sqlObject.addInput(new SQLInput("viewId", DataType.INTEGER));
		
		Map<String, Object> queryParameters = new HashMap<>();
		queryParameters.put("viewId", viewId);
		List<Map<String, Object>> columns = SQLInterface.executeQuery(sqlObject, queryParameters);
		
		for (Map<String, Object> map : columns) {
			String columnName = (String) map.get("COLUMN_NAME");
			//String dateTypeCode = (String) map.get("DATA_TYPE_CODE");
			//boolean keyYN = map.get("KEY_YN") != null && map.get("KEY_YN").equals("Y");
			String alias = (String) map.get("TABLE_ALIAS");
			String tableName = (String) map.get("TABLE_NAME");
			
			/*
			DataType dataType = DataType.STRING;
			if (dateTypeCode != null) {
				if (dateTypeCode.equals("N")) {
					dataType = DataType.INTEGER;
				}
			}
			
			CMEViewTableDef table = viewDef.getTableByAlias(alias);
			DBColumn column = DBSchemaInfo.getColumn(table.getTableName(), columnName);
			if (column != null) {
				dataType = column.getDataType();
			}
			
			CMEViewColumnDef columnDef = new CMEViewColumnDef(columnName, dataType, keyYN, alias);
			*/
			
			CMEViewColumnDef columnDef = new CMEViewColumnDef(columnName, tableName, alias);
			viewDef.addColumn(columnDef);
		}
	}
	
	/**
	 * Loads the join definitions for the CME view with the given ID into the given CMEViewDef.
	 * @param viewId ID of the CME view to load the column definitions for.
	 * @param viewDef CMEViewDef to load the column definitions into.
	 * TODO: Maybe make the CMEViewDef constructor call this?
	 */
	private static void loadCMEViewColumnJoins(final int viewId, CMEViewDef viewDef) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT jt.code_desc AS JOIN_TYPE_NAME, ");
		sb.append("jt.code_val AS JOIN_TYPE_CODE, ");
		sb.append("vs.alias AS TABLE_ALIAS, ");
		sb.append("stc.stcol_name AS COLUMN_NAME, ");
		sb.append("pvs.alias AS PARENT_TABLE_ALIAS, ");
		sb.append("pstc.stcol_name AS PARENT_COLUMN_NAME, ");
		sb.append("vj.sequence AS SEQUENCE ");
		sb.append("FROM t_ccs_admin_view_join vj ");
		//sb.append("JOIN t_ccs_admin_view_join_type jt ON jt.join_type_id = vj.join_type_id ");
		sb.append("JOIN t_ccs_admin_code_sys jt ON jt.code_id = vj.join_type_id ");
		sb.append("JOIN t_ccs_admin_view_stcol vsc ON vsc.viewstcol_id = vj.viewstcol_id ");
		sb.append("JOIN t_ccs_admin_view_stcol pvsc ON pvsc.viewstcol_id = vj.parent_viewstcol_id ");
		sb.append("JOIN t_ccs_admin_sys_tbl_col stc ON stc.stcol_id = vsc.stcol_id ");
		sb.append("JOIN t_ccs_admin_sys_tbl_col pstc ON pstc.stcol_id = pvsc.stcol_id ");
		sb.append("JOIN t_ccs_admin_view_stbl vs ON vs.viewstbl_id = vj.viewstbl_id ");
		sb.append("JOIN t_ccs_admin_view_stbl pvs ON pvs.viewstbl_id = vj.parent_viewstbl_id ");
		sb.append("WHERE vj.view_id = ? ");
		sb.append("ORDER BY vs.sequence");
		
		SQLObject sqlObject = new SQLObject();
		sqlObject.addSQL(sb.toString());
		sqlObject.addInput(new SQLInput("viewId", DataType.INTEGER));
		
		Map<String, Object> queryParameters = new HashMap<>();
		queryParameters.put("viewId", viewId);
		List<Map<String, Object>> joins = SQLInterface.executeQuery(sqlObject, queryParameters);
		
		for (Map<String, Object> map : joins) {
			String columnName = (String) map.get("COLUMN_NAME");
			String tableAlias = (String) map.get("TABLE_ALIAS");
			JoinType joinType = JoinType.getJoinTypeByCode((String) map.get("JOIN_TYPE_CODE"));
			String parentColumnName = (String) map.get("PARENT_COLUMN_NAME");
			String parentTableAlias = (String) map.get("PARENT_TABLE_ALIAS");
			
			CMEViewJoinDef join = new CMEViewJoinDef(columnName, tableAlias, joinType, parentColumnName, parentTableAlias);
			viewDef.addJoin(join);
		}
	}
	
	/**
	 * Loads the column predicate definitions for the CME view with the given ID into the given CMEViewDef.
	 * @param viewId ID of the CME view to load the column predicate definitions for.
	 * @param viewDef CMEViewDef to load the column predicate definitions into.
	 * TODO: Maybe make the CMEViewDef constructor call this?
	 */
	private static void loadCMEViewColumnPredicates(final int viewId, CMEViewDef viewDef) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT vs.alias AS TABLE_ALIAS, ");
		sb.append("vot.code_desc AS OPERATOR_NAME, ");
		sb.append("vot.code_val AS OPERATOR_CODE, ");
		sb.append("lvot.code_desc AS LEFT_OPERAND_TYPE_NAME, ");
		sb.append("lvot.code_val AS LEFT_OPERAND_TYPE_CODE, ");
		sb.append("lstc.stcol_name AS LEFT_OPERAND_COLUMN_NAME, ");
		sb.append("lvo.literal_string AS LEFT_OPERAND_LITERAL_STRING, ");
		sb.append("ldt.code_val AS LEFT_OPERAND_DATA_TYPE_CODE, ");
		sb.append("rvot.code_desc AS RIGHT_OPERAND_TYPE_NAME, ");
		sb.append("rvot.code_val AS RIGHT_OPERAND_TYPE_CODE, ");
		sb.append("rstc.stcol_name AS RIGHT_OPERAND_COLUMN_NAME, ");
		sb.append("rvo.literal_string AS RIGHT_OPERAND_LITERAL_STRING, ");
		sb.append("rdt.code_val AS RIGHT_OPERAND_DATA_TYPE_CODE, ");
		sb.append("vp.sequence AS SEQUENCE ");
		sb.append("FROM t_ccs_admin_view_pred vp ");
		sb.append("JOIN t_ccs_admin_view_stbl vs ON vs.viewstbl_id = vp.viewstbl_id ");
		sb.append("JOIN t_ccs_admin_code_sys vot ON vot.code_id = vp.operator_type_id ");
		sb.append("JOIN t_ccs_admin_view_operand lvo ON lvo.operand_id = vp.left_operand_id ");
		sb.append("JOIN t_ccs_admin_view_operand rvo ON rvo.operand_id = vp.right_operand_id ");
		sb.append("JOIN t_ccs_admin_code_sys lvot ON lvot.code_id = lvo.operand_type_id ");
		sb.append("JOIN t_ccs_admin_code_sys rvot ON rvot.code_id = rvo.operand_type_id ");
		sb.append("LEFT JOIN t_ccs_admin_view_stcol lvs ON lvs.viewstcol_id = lvo.viewstcol_id ");
		sb.append("LEFT JOIN t_ccs_admin_view_stcol rvs ON rvs.viewstcol_id = rvo.viewstcol_id ");
		sb.append("LEFT JOIN t_ccs_admin_sys_tbl_col lstc ON lstc.stcol_id = lvs.stcol_id ");
		sb.append("LEFT JOIN t_ccs_admin_sys_tbl_col rstc ON rstc.stcol_id = rvs.stcol_id ");
		sb.append("LEFT JOIN t_ccs_admin_code_sys ldt ON ldt.code_id = lvo.data_type_id ");
		sb.append("LEFT JOIN t_ccs_admin_code_sys rdt ON rdt.code_id = rvo.data_type_id ");
		sb.append("WHERE vp.view_id = ? ");
		sb.append("ORDER BY vp.sequence");

		SQLObject sqlObject = new SQLObject();
		sqlObject.addSQL(sb.toString());
		sqlObject.addInput(new SQLInput("viewId", DataType.INTEGER));
		
		Map<String, Object> queryParameters = new HashMap<>();
		queryParameters.put("viewId", viewId);
		List<Map<String, Object>> predicates = SQLInterface.executeQuery(sqlObject, queryParameters);
		
		for (Map<String, Object> map : predicates) {
			String alias = (String) map.get("TABLE_ALIAS");
			OperatorType operatorType = OperatorType.getOperatorTypeByCode((String) map.get("OPERATOR_CODE"));
			OperandType leftOperandType = OperandType.getOperandTypeByCode((String) map.get("LEFT_OPERAND_TYPE_CODE"));
			OperandType rightOperandType = OperandType.getOperandTypeByCode((String) map.get("RIGHT_OPERAND_TYPE_CODE"));
			String leftOperandColumnName = (String) map.get("LEFT_OPERAND_COLUMN_NAME");
			String leftOperandLiteralString = (String) map.get("LEFT_OPERAND_LITERAL_STRING");
			DataType leftDataType = DataType.getJoinTypeByCode((String) map.get("LEFT_OPERAND_DATA_TYPE_CODE"));
			String rightOperandColumnName = (String) map.get("RIGHT_OPERAND_COLUMN_NAME");
			String rightOperandLiteralString = (String) map.get("RIGHT_OPERAND_LITERAL_STRING");
			DataType rightDataType = DataType.getJoinTypeByCode((String) map.get("RIGHT_OPERAND_DATA_TYPE_CODE"));			
			
			CMEViewPredicateDef predicate = new CMEViewPredicateDef(alias, operatorType,
					new CMEViewOperandDef(leftOperandType, leftOperandColumnName, leftOperandLiteralString, leftDataType),
					new CMEViewOperandDef(rightOperandType, rightOperandColumnName, rightOperandLiteralString, rightDataType));
			viewDef.addPredicate(predicate);
		}
	}
	
	/**
	 * Loads the CME object-to-view associations.
	 * An "association" is what action on the object the view is used for: Create, Load, Update, Delete.
	 * @note This loads the associations into the already-loaded CMEObjectDefs.
	 * TODO: Call this at the end of loadCMEObjectDefs()?
	 */
	public static void loadCMEObjectViewAssociations() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT co.cmeobj_name AS CMEOBJECT_NAME, ");
		sb.append("co.cmeobj_id AS CMEOBJECT_ID, ");
		sb.append("v.view_name AS VIEW_NAME, ");
		sb.append("v.view_id AS VIEW_ID, ");
		sb.append("ca.code_val AS ACTION_CODE ");
		sb.append("FROM t_ccs_admin_cmeobj_view_rel cvr ");
		sb.append("JOIN t_ccs_admin_cme_object co ON co.cmeobj_id = cvr.cmeobj_id ");
		sb.append("JOIN t_ccs_admin_view v ON v.view_id = cvr.view_id ");
		sb.append("JOIN t_ccs_admin_code_sys ca ON ca.code_id = cvr.coaction_id ");
		sb.append("ORDER BY cvr.cmeobj_id, cvr.coaction_id, cvr.sequence");
		
		SQLObject sqlObject = new SQLObject();
		sqlObject.addSQL(sb.toString());
		
		List<Map<String, Object>> associations = SQLInterface.executeQuery(sqlObject);
		for (Map<String, Object> map : associations) {
			String cmeObjectName = (String) map.get("CMEOBJECT_NAME");
			int cmeObjectId = Integer.parseInt((String) map.get("CMEOBJECT_ID").toString());
			String viewName = (String) map.get("VIEW_NAME");
			int viewId = Integer.parseInt((String) map.get("VIEW_ID").toString());
			ObjectAction action = ObjectAction.getObjectActionByCode((String) map.get("ACTION_CODE"));
			
			CMEObjectDef cmeObjDef = CMEObjectDef.getCmeObjDef(cmeObjectName);
			CMEObjectViewRelationship viewRelationship = new CMEObjectViewRelationship(viewName);
			DefinitionLoader.loadCMEObjectColumnAssociations(cmeObjectId, viewId, viewRelationship);
			//System.out.println(cmeObjectName + ", " + viewName + ", " + action + ", " + map.get("ACTION_CODE"));
			cmeObjDef.addView(action, viewRelationship);
		}
	}
	
	/**
	 * Loads the CMEObject field-to-view-column associations.
	 * @param cmeObjectId ID of the CMEObject to load the associations for.
	 * @param viewId ID of the view to load the associations for.
	 * @param viewRelationship CMEObjectViewRelationship to load the associations into.
	 * TODO: Remove CMEOBJ_NAME, VIEW_NAME, and TABLE_ALIAS from the SELECT list?
	 */
	public static void loadCMEObjectColumnAssociations(final int cmeObjectId, final int viewId, 
			CMEObjectViewRelationship viewRelationship) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT co.cmeobj_name AS CMEOBJ_NAME, ");
		sb.append("v.view_name AS VIEW_NAME, ");
		sb.append("cc.cocol_name AS CMEOBJCOL_NAME, ");
		sb.append("stc.stcol_name AS VIEWCOL_NAME, ");
		sb.append("vst.alias AS TABLE_ALIAS ");
		sb.append("FROM t_ccs_admin_cocol_vstcol_rel cvr ");
		sb.append("JOIN t_ccs_admin_cmeobj_column cc ON cc.cocol_id = cvr.cocol_id ");
		sb.append("JOIN t_ccs_admin_cme_object co ON co.cmeobj_id = cc.cmeobj_id AND co.cmeobj_id = ? ");
		sb.append("JOIN t_ccs_admin_view_stcol vs ON vs.viewstcol_id = cvr.viewstcol_id ");
		sb.append("JOIN t_ccs_admin_sys_tbl_col stc ON stc.stcol_id = vs.stcol_id ");
		sb.append("JOIN t_ccs_admin_view_stbl vst ON vst.viewstbl_id = vs.viewstbl_id ");
		sb.append("JOIN t_ccs_admin_view v ON v.view_id = vst.view_id AND v.view_id = ?");
		
		SQLObject sqlObject = new SQLObject();
		sqlObject.addSQL(sb.toString());
		sqlObject.addInput(new SQLInput("cmeObjectId", DataType.INTEGER));
		sqlObject.addInput(new SQLInput("viewId", DataType.INTEGER));
		
		Map<String, Object> queryParameters = new HashMap<>();
		queryParameters.put("cmeObjectId", cmeObjectId);
		queryParameters.put("viewId", viewId);
		List<Map<String, Object>> associations = SQLInterface.executeQuery(sqlObject, queryParameters);
		
		for (Map<String, Object> map : associations) {
			//String cmeObjectName = (String) map.get("CMEOBJ_NAME");
			//String viewName = (String) map.get("VIEW_NAME");
			String cmeObjColName = (String) map.get("CMEOBJCOL_NAME");
			String viewColName = (String) map.get("VIEWCOL_NAME");
			//String tableAlias = (String) map.get("TABLE_ALIAS");
			
			viewRelationship.addRelationship(cmeObjColName, viewColName);
		}
	}
	
	/**
	 * Loads the CMEObject parent-to-child associations.
	 */
	public static void loadCMEObjectChildAssociations() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT c.cochild_id AS CHILD_ID, ");
		sb.append("po.cmeobj_name AS PARENT_OBJ_NAME, ");
		sb.append("co.cmeobj_name AS CHILD_OBJ_NAME, ");
		sb.append("cc.cocol_name AS REF_COL_NAME, ");
		sb.append("c.single_group_ind AS SINGLE_GROUP_IND, ");
		sb.append("c.hide_parent_yn AS HIDE_PARENT_YN ");
		sb.append("FROM t_ccs_admin_cmeobj_child c ");
		sb.append("JOIN t_ccs_admin_cme_object po ON po.cmeobj_id = c.cmeobj_id ");
		sb.append("JOIN t_ccs_admin_cme_object co ON co.cmeobj_id = c.child_cmeobj_id ");
		sb.append("JOIN t_ccs_admin_cmeobj_column cc ON cc.cocol_id = c.ref_cocol_id");
		
		SQLObject sqlObject = new SQLObject();
		sqlObject.addSQL(sb.toString());
		
		List<Map<String, Object>> associations = SQLInterface.executeQuery(sqlObject);
		for (Map<String, Object> map : associations) {
			int childId = Integer.parseInt((String) map.get("CHILD_ID").toString());
			String parentObjName = (String) map.get("PARENT_OBJ_NAME");
			String childObjName = (String) map.get("CHILD_OBJ_NAME");
			String refColName = (String) map.get("REF_COL_NAME");
			CMEObjectType singleGroupType = CMEObjectType.getCMEObjectTypeByCode((String) map.get("SINGLE_GROUP_IND"));
			boolean hideParentYN = map.get("HIDE_PARENT_YN") != null && map.get("HIDE_PARENT_YN").equals("Y");

			CMEObjectDef cmeObjDef = CMEObjectDef.getCmeObjDef(parentObjName);
			CMEObjectChildDef childDef = new CMEObjectChildDef(childObjName, refColName, singleGroupType, hideParentYN);
			DefinitionLoader.loadCMEObjectChildColumnAssociations(childId, childDef);
			cmeObjDef.addChildDef(childDef);
		}
	}
	
	/**
	 * Loads the CMEObject parent-to-child key column associations.
	 * @param childId ID of the child to load the associations for.
	 * @param childDef CMEObjectChildDef to load the associations into.
	 */
	public static void loadCMEObjectChildColumnAssociations(final int childId, CMEObjectChildDef childDef) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT pc.cocol_name AS PARENT_COLUMN_NAME, ");
		sb.append("cc.cocol_name AS CHILD_COLUMN_NAME ");
		sb.append("FROM t_ccs_admin_cochild_cocol_rel ccr ");
		sb.append("JOIN t_ccs_admin_cmeobj_column pc ON pc.cocol_id = ccr.cocol_id ");
		sb.append("JOIN t_ccs_admin_cmeobj_column cc ON cc.cocol_id = ccr.child_cocol_id ");
		sb.append("WHERE ccr.cochild_id = ?");
		
		SQLObject sqlObject = new SQLObject();
		sqlObject.addSQL(sb.toString());
		sqlObject.addInput(new SQLInput("cochild_id", DataType.INTEGER));
		
		Map<String, Object> queryParameters = new HashMap<>();
		queryParameters.put("cochild_id", childId);
		List<Map<String, Object>> associations = SQLInterface.executeQuery(sqlObject, queryParameters);
		
		for (Map<String, Object> map : associations) {
			String parentColName = (String) map.get("PARENT_COLUMN_NAME");
			String childColName = (String) map.get("CHILD_COLUMN_NAME");
			childDef.addKey(new CMEChildRelKeysDef(childColName, parentColName));	
		}
	}
}
