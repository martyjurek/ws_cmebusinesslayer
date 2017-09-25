package com.misys.cmeobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.misys.cmeobject.search.SQLQueryBuilder;
import com.misys.definitions.CMEObjectColumnDef;
import com.misys.definitions.CMEObjectDef;
import com.misys.definitions.CMEViewColumnDef;
import com.misys.definitions.CMEViewDef;
import com.misys.definitions.CMEViewSQLBuilder;
import com.misys.definitions.CMEViewPredicateDef;
import com.misys.enums.ObjectAction;
import com.misys.jdbc.SQLInterface;
import com.misys.jdbc.SQLObject;

public class CMEObjectDAO<T> {
	private static Logger logger = LogManager.getLogger(CMEObjectDAO.class);

    /*
	public static List<Map<String, Object>> search(CMEViewDef view, List<CMEObjectColumnDef> columns, 
			List<CMEViewTablePredicateDef> additionalPredicates, Map<String, Object> parameters) {
		SQLObject sqlObject = CMEViewSQLBuilder.getSQLObjectFromView(view, columns, additionalPredicates);
		return SQLInterface.executeQuery(sqlObject, new ArrayList<Object>(parameters.values()));
	}
     */

    /*
	public static List<CMEObject> search(String type, Map<String, Object> parameters) {
		CMEObjectDef def = CMEObjectDef.getCmeObjDef(type);
		CMEViewDef view = def.getViewByAction(ObjectAction.SEARCH);
		List<CMEViewTablePredicateDef> additionalPredicates = CMEObjectDAO.getAdditionalPredicates(view.getAllPredicates(), parameters);
		SQLObject sqlObject = CMEViewSQLBuilder.getSQLObjectFromView(view, def.getColumns(), additionalPredicates);
		List<Map<String, Object>> results = SQLInterface.executeQuery(sqlObject, new ArrayList<Object>(parameters.values()));
		List<CMEObject> objects = new ArrayList<CMEObject>();
		for (Map<String, Object> values : results) {
			CMEObject cmeObject = new CMEObject(type, values);
			objects.add(cmeObject);
		}
		return objects;
	}
     */
    /*
	@SuppressWarnings("unchecked")
	public T executeView(CMEViewDef view, List<CMEViewColumnDef> columns, List<CMEViewPredicateDef> additionalPredicates, 
			Map<String, Object> parameters) {

		SQLObject sqlObject = CMEViewSQLBuilder.getSQLObjectFromView(view, columns, additionalPredicates);

		switch (view.getAction()) {
			case SEARCH: 
				return (T) SQLInterface.executeQuery(sqlObject, parameters);
			case LOAD:
			case CREATE:
				return (T) SQLInterface.executeQuerySingleRow(sqlObject, parameters);
			case UPDATE:
			case DELETE:
			default:
				return (T) SQLInterface.execute(sqlObject, parameters);
		}
	}
     */

    public static Map<String, Object> getSingle(CMEViewDef view, List<CMEViewColumnDef> columns, 
            List<CMEViewPredicateDef> predicates, Map<String, Object> parameters) {

        SQLObject sqlObject = CMEViewSQLBuilder.getSQLObjectFromView(view, columns, predicates);
        logger.trace(sqlObject.getSQL() + ", " + sqlObject.getInputs());
        return SQLInterface.executeQuerySingleRow(sqlObject, parameters);
    }

    public static List<Map<String, Object>> getMultiple(CMEViewDef view, List<CMEViewColumnDef> columns, 
            List<CMEViewPredicateDef> predicates, Map<String, Object> parameters) {

        SQLObject sqlObject = CMEViewSQLBuilder.getSQLObjectFromView(view, columns, predicates);
        logger.trace(sqlObject.getSQL() + ", " + sqlObject.getInputs());
        return SQLInterface.executeQuery(sqlObject, parameters);
    }

    public static List<Map<String, Object>> getMultiple(CMEViewDef view, List<CMEViewColumnDef> columns, SQLQueryBuilder builder) {
        SQLObject sqlObject = CMEViewSQLBuilder.getSQLObjectFromQuery(view, columns, builder);
        logger.trace(sqlObject.getSQL());
        return SQLInterface.executeQuery(sqlObject, builder.getParameters());
    }

    public static boolean update(CMEViewDef view, List<CMEViewColumnDef> columns, 
            List<CMEViewPredicateDef> predicates, Map<String, Object> parameters) {

        SQLObject sqlObject = CMEViewSQLBuilder.getSQLObjectFromView(view, columns, predicates);
        logger.trace(sqlObject.getSQL() + ", " + sqlObject.getInputs());
        return SQLInterface.execute(sqlObject, parameters);
    }

    /*
	public static Map<String, Object> get(CMEViewDef view, List<CMEObjectColumnDef> columns, Map<String, Object> parameters) {
		SQLObject sqlObject = CMEViewSQLBuilder.getSQLObjectFromView(view, columns, null);
		return SQLInterface.executeQuerySingleRow(sqlObject, new ArrayList<Object>(parameters.values()));
	}
     */

    /*
	public static CMEObject get(String type, Map<String, Object> parameters) {
		CMEObjectDef def = CMEObjectDef.getCmeObjDef(type);
		CMEViewDef view = def.getViewByAction(ObjectAction.LOAD);
		SQLObject sqlObject = CMEViewSQLBuilder.getSQLObjectFromView(view, def.getColumns(), null);
		Map<String, Object> values = SQLInterface.executeQuerySingleRow(sqlObject, new ArrayList<Object>(parameters.values()));
		CMEObject cmeObject = new CMEObject(type, values);
		return cmeObject;
	}
     */

    /*
	public static void update(CMEObject cmeObject) {
		CMEObjectDef def = CMEObjectDef.getCmeObjDef(cmeObject.getType());
		CMEViewDef view = def.getViewByAction(ObjectAction.UPDATE);
		SQLObject sqlObject = CMEViewSQLBuilder.getSQLObjectFromView(view, def.getColumns(), null);
		SQLInterface.execute(sqlObject, new ArrayList<Object>(cmeObject.getValues().values()));
	}

	public static CMEObject create(CMEObject cmeObject) {
		CMEObjectDef def = CMEObjectDef.getCmeObjDef(cmeObject.getType());
		CMEViewDef view = def.getViewByAction(ObjectAction.CREATE);
		SQLObject sqlObject = CMEViewSQLBuilder.getSQLObjectFromView(view, def.getColumns(), null);
		Map<String, Object> values = SQLInterface.executeQuerySingleRow(sqlObject, new ArrayList<Object>(cmeObject.getValues().values()));
		for (CMEObjectColumnDef column : def.getKeyColumns()) {
			cmeObject.setInitialValue(column.getColumnName(), values.get(column.getColumnName()));
		}
		return cmeObject;
	}

	public static void delete(CMEObject cmeObject) {
		CMEObjectDef def = CMEObjectDef.getCmeObjDef(cmeObject.getType());
		CMEViewDef view = def.getViewByAction(ObjectAction.DELETE);
		SQLObject sqlObject = CMEViewSQLBuilder.getSQLObjectFromView(view, null, null);
		SQLInterface.execute(sqlObject, new ArrayList<Object>(cmeObject.getValues().values()));
	}
     */
    /*
	private static List<CMEViewTablePredicateDef> getAdditionalPredicates(List<CMEViewTablePredicateDef> predicates, Map<String, Object> parameters) {
		List<CMEViewTablePredicateDef> additionalPredicates = null;
		if (predicates.size() > 0 && parameters.size() > 0 && predicates.size() == parameters.size()) {
			additionalPredicates = new ArrayList<CMEViewTablePredicateDef>();
		}
		return additionalPredicates;
	}
     */
}
