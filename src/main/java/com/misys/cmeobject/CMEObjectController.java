package com.misys.cmeobject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.misys.cmeobject.search.Queries.Query;
import com.misys.cmeobject.search.QueryException;
import com.misys.cmeobject.search.SQLQueryBuilder;
import com.misys.definitions.CMEChildRelKeysDef;
import com.misys.definitions.CMEObjectChildDef;
import com.misys.definitions.CMEObjectDef;
import com.misys.definitions.CMEObjectViewRelationship;
import com.misys.definitions.CMEViewColumnDef;
import com.misys.definitions.CMEViewDef;
import com.misys.definitions.CMEViewOperandDef;
import com.misys.definitions.CMEViewPredicateDef;
import com.misys.enums.CMEObjectType;
import com.misys.enums.DataType;
import com.misys.enums.ObjectAction;
import com.misys.enums.OperandType;
import com.misys.enums.OperatorType;
import com.misys.jdbc.DBSchemaInfo;

public class CMEObjectController {

    public static List<CMEObject> search(String name, String viewName, List<String> columnNames, Query query) {
        List<CMEObject> results = new ArrayList<>();

        CMEObjectDef objectDef = CMEObjectDef.getCmeObjDef(name);
        CMEObjectViewRelationship viewRelationship = null;

        if (viewName != null) {
            viewRelationship = objectDef.getViewByName(ObjectAction.SEARCH, viewName);
        } else {
            viewRelationship = objectDef.getViews(ObjectAction.SEARCH).get(0);
        }
        CMEViewDef view = CMEViewDef.getViewDef(viewRelationship.getViewName());

        SQLQueryBuilder.Join.resetRandomizer();
        SQLQueryBuilder sqlBuilder = new SQLQueryBuilder(view, objectDef, viewRelationship);
        try {
            query.getOperator().traverse(sqlBuilder);
        } catch (QueryException e) {
            throw new RuntimeException(e);
        }
        List<CMEViewColumnDef> columns = CMEObjectController.translateFromCmeObjectColumns(view, viewRelationship, columnNames);

        List<Map<String, Object>> resultList = CMEObjectDAO.getMultiple(view, columns, sqlBuilder);
        for (Map<String, Object> result : resultList) {
            CMEObject cmeObject = new CMEObject(objectDef.getName());
            CMEObjectController.populateCMEObject(cmeObject, viewRelationship, result);
            results.add(cmeObject);
        }
        return results;
    }

    public static List<CMEObject> search(String name, String viewName, List<String> columnNames, Map<String, Object> parameters) {

        List<CMEObject> results = new ArrayList<CMEObject>();

        CMEObjectDef objectDef = CMEObjectDef.getCmeObjDef(name);
        CMEObjectViewRelationship viewRelationship = null;

        if (viewName != null) {
            viewRelationship = objectDef.getViewByName(ObjectAction.SEARCH, viewName);
        } else {
            //Get the first one by default
            //System.out.println(objectDef.getViews(ObjectAction.SEARCH));
            viewRelationship = objectDef.getViews(ObjectAction.SEARCH).get(0);
        }

        CMEViewDef view = CMEViewDef.getViewDef(viewRelationship.getViewName());
        List<CMEViewColumnDef> columns = 
                CMEObjectController.translateFromCmeObjectColumns(view, viewRelationship, columnNames);

        parameters.putAll(objectDef.getDefaultValues());
        List<CMEViewPredicateDef> predicates = new ArrayList<CMEViewPredicateDef>(view.getPredicates());
        for (String paramName : parameters.keySet()) {
            //System.out.println(paramName);
            String viewColName = viewRelationship.getViewColName(paramName);
            //System.out.println(viewRelationship.getViewColNames());
            //System.out.println(paramName + ": " + viewColName);
            if (!view.containsPredicateColumnName(viewColName)) {
                CMEViewColumnDef viewColumn = view.getColumnByName(viewColName);
                //System.out.println(viewColName + ": " + viewColumn);
                DataType dataType = DBSchemaInfo.getColumn(viewColumn.getTableName(), viewColName).getDataType();
                CMEViewPredicateDef predicate = new CMEViewPredicateDef(viewColumn.getTableAlias(), OperatorType.EQUALS,
                        new CMEViewOperandDef(OperandType.COLUMN, viewColName, null, dataType), 
                        new CMEViewOperandDef(OperandType.INPUT, viewColName, null, dataType));
                predicates.add(predicate);
            }
        }

        List<Map<String, Object>> list = CMEObjectDAO.getMultiple(view, columns, predicates, parameters);

        for (Map<String, Object> map : list) {
            CMEObject cmeObject = new CMEObject(objectDef.getName());
            CMEObjectController.populateCMEObject(cmeObject, viewRelationship, map);
            //processChildren(cmeObject);
            results.add(cmeObject);
        }
        return results;		
    }

    public static CMEObject get(String name, List<String> columnNames, Map<String, Object> parameters, boolean loadReferences) {

        CMEObjectDef objectDef = CMEObjectDef.getCmeObjDef(name);
        CMEObject cmeObject = new CMEObject(objectDef.getName());

        List<CMEObjectViewRelationship> viewRelationships = objectDef.getViews(ObjectAction.LOAD);
        for (CMEObjectViewRelationship viewRelationship : viewRelationships) {	

            CMEViewDef view = CMEViewDef.getViewDef(viewRelationship.getViewName());
            List<CMEViewColumnDef> columns = 
                    CMEObjectController.translateFromCmeObjectColumns(view, viewRelationship, columnNames);

            parameters.putAll(objectDef.getDefaultValues());
            Map<String, Object> map = CMEObjectDAO.getSingle(view, columns, view.getPredicates(), parameters);
            if (map.size() > 0) {
                CMEObjectController.populateCMEObject(cmeObject, viewRelationship, map);
            }

            if (loadReferences) {
                //processChildren(cmeObject);
            }
        }
        if (cmeObject.getValues().size() > 0) {
            return cmeObject;
        } else {
            return null;
        }
    }

    public static String getObjectNameForPath(String path) {
        return CMEObjectDef.getCmeObjNameByPath(path);
    }

    private static void processChildren(CMEObject cmeObject) {
        CMEObjectDef objectDef = CMEObjectDef.getCmeObjDef(cmeObject.getType());
        if (objectDef.hasChildDefs()) {
            for (CMEObjectChildDef childDef : objectDef.getChildDefs()) {
                String referenceObjName = childDef.getChildName();
                String referenceColName = childDef.getRefColName();
                //System.out.println(referenceObjName + ", " + referenceColName);
                Map<String, Object> refMap = new HashMap<String, Object>();
                for (CMEChildRelKeysDef childKey : childDef.getKeys()) {
                    //System.out.println("here: " + childKey.getColumnName() + ", " + childKey.getParentColumnName());
                    refMap.put(childKey.getColumnName(), cmeObject.getValue(childKey.getParentColumnName()));
                }
                //System.out.println(refMap);
                if (childDef.getType().equals(CMEObjectType.GROUP)) {
                    List<CMEObject> reference = CMEObjectController.search(referenceObjName, 
                            null, null, refMap);
                    cmeObject.setInitialValue(referenceColName, reference);
                } else {
                    CMEObject reference = CMEObjectController.search(referenceObjName, 
                            null, null, refMap).get(0);
                    cmeObject.setInitialValue(referenceColName, reference);
                }
            }
        }
    }

    public static void update(CMEObject cmeObject) {

        CMEObjectDef objectDef = CMEObjectDef.getCmeObjDef(cmeObject.getType());

        List<CMEObjectViewRelationship> viewRelationships = objectDef.getViews(ObjectAction.UPDATE);
        for (CMEObjectViewRelationship viewRelationship : viewRelationships) {	

            CMEViewDef view = CMEViewDef.getViewDef(viewRelationship.getViewName());

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.putAll(objectDef.getDefaultValues());
            for (Map.Entry<String, Object> entry : cmeObject.getDirtyValues().entrySet()) {
                parameters.put(viewRelationship.getViewColName(entry.getKey()), entry.getValue());
            }

            List<String> viewColNames = viewRelationship.getViewColNames(new ArrayList<String>(cmeObject.getDirtyValues().keySet()));
            if (viewColNames != null && viewColNames.size() > 0) {
                List<CMEViewColumnDef> columns = view.getColumnsByNames(viewColNames);

                List<String> predicateColumnNames = view.getPredicateColumnNames();
                for (String predicateColumnName : predicateColumnNames) {
                    String cmeObjColName = viewRelationship.getCmeObjColName(predicateColumnName);
                    parameters.put(predicateColumnName, cmeObject.getValue(cmeObjColName));
                }

                CMEObjectDAO.update(view, columns, view.getPredicates(), parameters);
            }
        }
    }

    public static CMEObject insert(CMEObject cmeObject) {

        CMEObjectDef objectDef = CMEObjectDef.getCmeObjDef(cmeObject.getType());

        List<CMEObjectViewRelationship> viewRelationships = objectDef.getViews(ObjectAction.CREATE);
        for (CMEObjectViewRelationship viewRelationship : viewRelationships) {	

            CMEViewDef view = CMEViewDef.getViewDef(viewRelationship.getViewName());

            List<String> viewColNames = viewRelationship.getViewColNames(new ArrayList<String>(cmeObject.getValues().keySet()));

            Map<String, Object> parameters = new HashMap<String, Object>();

            Map<String, Object> defaults = objectDef.getDefaultValues();
            if (defaults.size() > 0) {
                parameters.putAll(defaults);
                viewColNames.addAll(defaults.keySet());
            }
            for (Map.Entry<String, Object> entry : cmeObject.getValues().entrySet()) {
                String key = viewRelationship.getViewColName(entry.getKey());
                //if (!parameters.containsKey(key)) {
                parameters.put(key, entry.getValue());
                //}
            }
            List<CMEViewColumnDef> columns = view.getColumnsByNames(viewColNames);

            Map<String, Object> map = CMEObjectDAO.getSingle(view, columns, null, parameters);
            CMEObjectController.populateCMEObject(cmeObject, viewRelationship, map);
        }
        return cmeObject;		
    }

    public static void delete(String name, Map<String, Object> parameters) {
        CMEObjectDef objectDef = CMEObjectDef.getCmeObjDef(name);		
        List<CMEObjectViewRelationship> viewRelationships = objectDef.getViews(ObjectAction.DELETE);
        for (CMEObjectViewRelationship viewRelationship : viewRelationships) {	
            CMEViewDef view = CMEViewDef.getViewDef(viewRelationship.getViewName());
            Map<String, Object> viewParameters = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                viewParameters.put(viewRelationship.getViewColName(entry.getKey()), entry.getValue());
            }
            CMEObjectDAO.update(view, null, view.getPredicates(), viewParameters);
        }
    }

    private static List<CMEViewColumnDef> translateFromCmeObjectColumns(CMEViewDef view, 
            CMEObjectViewRelationship viewRelationship,	List<String> columnNames) {

        if (columnNames != null && columnNames.size() > 0) {
            List<CMEViewColumnDef> columns = view.getColumnsByNames(viewRelationship.getViewColNames(columnNames));
            for (String predicateColumnNames : view.getPredicateColumnNames()) {
                if (!columns.contains(predicateColumnNames)) {
                    columns.add(view.getColumnByName(viewRelationship.getViewColName(predicateColumnNames)));
                }
            }
            for (CMEViewColumnDef column : view.getKeyColumns()) {
                if (!columns.contains(column.getColumnName())) {
                    columns.add(column);
                }
            }
            return columns;
        } else {
            return view.getColumns();
        }
    }

    private static void populateCMEObject(CMEObject cmeObject, CMEObjectViewRelationship viewRelationship, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = viewRelationship.getCmeObjColName(entry.getKey());
            //System.out.println(entry.getKey() + ", " + key);
            if (key != null) {
                cmeObject.setInitialValue(key, entry.getValue());
            }
        }
    }

    public static String getPath(String name) {
        CMEObjectDef objectDef = CMEObjectDef.getCmeObjDef(name);
        return objectDef.getPath();
    }

    public static List<CMEObjectChildInfo> getChildInfo(CMEObject cmeObject) {
        CMEObjectDef objectDef = CMEObjectDef.getCmeObjDef(cmeObject.getType());
        List<CMEObjectChildInfo> list = new ArrayList<CMEObjectChildInfo>();
        for (CMEObjectChildDef childDef : objectDef.getChildDefs()) {
            CMEObjectChildInfo childInfo = new CMEObjectChildInfo();
            childInfo.setChildType(childDef.getChildName());
            childInfo.setParentRefColName(childDef.getRefColName());
            for (CMEChildRelKeysDef key : childDef.getKeys()) {
                childInfo.addKey(key.getColumnName(), cmeObject.getValue(key.getParentColumnName()));
            }
            list.add(childInfo);
        }
        return list;
    }

}
