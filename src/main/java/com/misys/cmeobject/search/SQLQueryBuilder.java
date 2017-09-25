package com.misys.cmeobject.search;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.misys.cmeobject.search.Queries.BinaryConditionalOperator;
import com.misys.cmeobject.search.Queries.Combinator;
import com.misys.cmeobject.search.Queries.CombinatorNode;
import com.misys.cmeobject.search.Queries.ConditionalOperator;
import com.misys.cmeobject.search.Queries.GroupMatchOperator;
import com.misys.cmeobject.search.Queries.MatchType;
import com.misys.cmeobject.search.Queries.NAryConditionalOperator;
import com.misys.cmeobject.search.Queries.Query;
import com.misys.cmeobject.search.Queries.QueryNode;
import com.misys.cmeobject.search.Queries.Selector;
import com.misys.cmeobject.search.Queries.SelectorNode;
import com.misys.cmeobject.search.Queries.ValueNode;
import com.misys.cmeobject.search.Queries.ValueType;
import com.misys.cmeobject.search.Queries.WildcardConditionalOperator;
import com.misys.definitions.CMEChildRelKeysDef;
import com.misys.definitions.CMEObjectChildDef;
import com.misys.definitions.CMEObjectDef;
import com.misys.definitions.CMEObjectViewRelationship;
import com.misys.definitions.CMEViewColumnDef;
import com.misys.definitions.CMEViewDef;
import com.misys.definitions.CMEViewJoinDef;
import com.misys.enums.CMEObjectType;
import com.misys.enums.JoinType;
import com.misys.enums.ObjectAction;
import com.misys.enums.ViewType;

/**
 * A QueryBuilder implementation that converts a query tree into SQL syntax querying against a specific CMEView.
 */
public class SQLQueryBuilder implements QueryBuilder {
    private StringBuilder whereBuilder;
    private List<ValueNode> parameters;
    private CMEViewDef view;
    private Map<String, Join> childJoins; 
    private CMEObjectDef objDef;
    private CMEObjectViewRelationship objViewRel;
    private boolean negative;
    private String overrideAlias = null;
    
    public SQLQueryBuilder(CMEViewDef view, CMEObjectDef objDef, CMEObjectViewRelationship objViewRel, Map<String, Join> childJoins, boolean negative) {
        this.view = view;
        this.objDef = objDef;
        this.objViewRel = objViewRel;
        this.negative = negative;

        whereBuilder = new StringBuilder();
        parameters = new ArrayList<>();
        if (childJoins == null) {
            this.childJoins = new LinkedHashMap<>();
        } else {
            this.childJoins = childJoins;
        }
    }

    public SQLQueryBuilder(CMEViewDef view, CMEObjectDef objDef,CMEObjectViewRelationship objViewRel,  Map<String, Join> childJoins) {
        this(view, objDef, objViewRel, childJoins, false);
    }
    
    /**
     * @param view a CMEViewDef for the view to be queried.
     * @param objDef a CMEObjectDef for the object to be queried.
     * @param objViewRel a CMEObjectViewRelationship for the view and CMEObject being queried.
     */
    public SQLQueryBuilder(CMEViewDef view, CMEObjectDef objDef, CMEObjectViewRelationship objViewRel) {
        this(view, objDef, objViewRel, null, false);
    }

    public StringBuilder getWhereBuilder() {
        return whereBuilder;
    }
    
    public StringBuilder getJoinBuilder() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Join join : childJoins.values()) {
            if (!first) {
                sb.append(" ");
            }
            sb.append(join.build());
            first = false;
        }
        return sb;
    }

    public CMEViewDef getView() {
        return view;
    }

    public Map<String, Join> getChildJoins() {
        return childJoins;
    }

    public CMEObjectDef getObjDef() {
        return objDef;
    }

    public CMEObjectViewRelationship getObjViewRel() {
        return objViewRel;
    }

    public boolean isNegative() {
        return negative;
    }

    public List<ValueNode> getParameters() {
        return parameters;
    }

    public String getOverrideAlias() {
        return overrideAlias;
    }
    
    public void setOverrideAlias(String overrideAlias) {
        this.overrideAlias = overrideAlias;
    }
    
    /**
     * Adds a simple selection statement to the where clause (=, != IN(), LIKE, etc)
     * @param node the QueryNode representing a selection operation
     */
    @Override
    public void append(SelectorNode node) throws QueryException {
        String field = node.getFieldName();
        ConditionalOperator operator = node.getOperator();
        if (Selector.ELEMMATCH.equals(operator.getType())) {
            // Special handling for group matches
            processGroupMatch(field, (GroupMatchOperator)operator);         
        } else {
            appendColumn(field);
            append(operator);
        }
    }

    private void processGroupMatch(String field, GroupMatchOperator operator) throws QueryException {
        String parent = null;
        String [] fieldParts = field.split("\\.");
        for (int iii = 0; iii < fieldParts.length - 1; iii++) {
            String prevParent = parent;
            parent = fieldParts[iii];
            buildJoin(prevParent, parent);
        }
        String groupRefName = fieldParts[fieldParts.length - 1];
        buildJoin(parent, groupRefName);
        Join groupJoin = childJoins.get(groupRefName);
        if(!CMEObjectType.GROUP.equals(groupJoin.getLogicalType())) {
            throw new QueryException("CMEBADQUERY", "400", "Malformed query: $elemMatch on non-group field");
        }
        CMEObjectDef groupObj = groupJoin.getObject();
        List<CMEObjectChildDef> groupChildren = groupObj.getChildDefs();
        if (groupChildren == null || groupChildren.isEmpty()) {
            throw new QueryException("CMEGENERR", "500", "Failed to generate $elemMatch query");
        }
        boolean first = true;
        QueryNode queryNode = operator.getFilter().getOperator();
        for (CMEObjectChildDef child : groupChildren) {
            String childRefName = child.getRefColName();
            String childObjName = child.getChildName();
            buildJoin(groupRefName, childRefName);
            String alias = childJoins.get(childRefName).getChildAlias();
            CMEObjectDef childObj = CMEObjectDef.getCmeObjDef(childObjName);
            CMEObjectViewRelationship childViewRel = childObj.getViews(ObjectAction.SEARCH).get(0);
            CMEViewDef childView = CMEViewDef.getViewDef(childViewRel.getViewName());
            SQLQueryBuilder childBuilder = new SQLQueryBuilder(childView, childObj, childViewRel);
            childBuilder.setOverrideAlias(alias);
            try {
                queryNode.traverse(childBuilder);
                addSpace();
                if (!first) {
                    whereBuilder.append("OR ");
                }
                boolean subQuery = (queryNode instanceof CombinatorNode && groupChildren.size() > 1);
                if (subQuery) {
                    whereBuilder.append("(");
                }
                merge(childBuilder);
                if (subQuery) {
                    whereBuilder.append(")");
                }
                first = false;
            } catch (QueryException ex) {
                // XXX add warning log here
            }
        }
    }
    
    /**
     * Adds a compound boolean statement to the where clause (AND, OR, NOT)
     * @param node the QueryNode representing the boolean operation
     * @throws QueryException if the operation would generate illegal SQL, such as an empty AND operation
     */
    @Override
    public void append(CombinatorNode node) throws QueryException {
        if (Combinator.NOT.equals(node.getCombinatorType())) {
            negate(node);
        } else {
            appendBoolean(node);            
        }
    }
    
    private void appendBoolean(CombinatorNode node) throws QueryException {
        Combinator combinator = node.getCombinatorType();
        addSpace();
        boolean setNot = (negative && !Combinator.NAND.equals(combinator) && !Combinator.NOR.equals(combinator))
                || (!negative && (Combinator.NAND.equals(combinator) || Combinator.NOR.equals(combinator))); 
        if (setNot) {
            whereBuilder.append("NOT (");
        }
        List<Query> children = node.getChildren();
        if (children.size() == 0) {
            throw new QueryException("CMEBADQUERY", "400", "This query is malformed.");
        } else if (children.size() == 1) {
            System.err.println("WARNING: Combinator only has one child and is a noop: " + node);
        }
        boolean first = true;
        for (Query child : children) {
            if (!first) {
                whereBuilder.append(" ");
                whereBuilder.append(getSQLBoolean(combinator));
                whereBuilder.append(" ");
            }
            boolean subCombinator = isSubCombinator(child.getOperator());
            if (subCombinator) {
                whereBuilder.append("(");
            }
            SQLQueryBuilder csb = new SQLQueryBuilder(view, objDef, objViewRel, childJoins);
            csb.setOverrideAlias(overrideAlias);
            child.getOperator().traverse(csb);
            merge(csb);
            if (subCombinator) {
                whereBuilder.append(")");
            }
            first = false;
        }
        if(setNot) {
            whereBuilder.append(")");
        }
    }

    private boolean isSubCombinator(QueryNode operator) {
        if (operator instanceof CombinatorNode) {
            return true;
        }
        if (operator instanceof SelectorNode) {
            ConditionalOperator cond = ((SelectorNode)operator).getOperator();
            if (cond instanceof GroupMatchOperator) {
                Query child = ((GroupMatchOperator)cond).getFilter();
                return isSubCombinator(child.getOperator());
            }
        }
        return false;
    }
    
    /**
     * Special handling for NOT operators
     * @param node the QueryNode representing the NOT operation
     */
    private void negate(CombinatorNode node) throws QueryException {
        SQLQueryBuilder sqb = new SQLQueryBuilder(view, objDef, objViewRel, childJoins, !negative);
        sqb.setOverrideAlias(overrideAlias);
        List<Query> children = node.getChildren();
        if (children.size() == 1) {
            children.get(0).getOperator().traverse(sqb);
        } else {
            throw new QueryException("CMEBADQUERY", "400", "This query is malformed.");
        }
        addSpace();
        this.merge(sqb);
    }

    /**
     * Generic handling for non-boolean operators
     */
    public void append(ConditionalOperator operator) throws QueryException {
        if (operator instanceof BinaryConditionalOperator) {
            if (operator instanceof WildcardConditionalOperator) {
                append((WildcardConditionalOperator)operator);
            } else {
                append((BinaryConditionalOperator)operator);
            }
        } else if (operator instanceof NAryConditionalOperator) {
            append((NAryConditionalOperator)operator);
        } else if (operator instanceof GroupMatchOperator) {
            append((GroupMatchOperator)operator);
        } else {
            throw new UnsupportedOperationException();
        }        
    }

    /**
     * Special logic for simple binary operators (=, != >. <. etc)
     * @param operator the binary operator
     * @throws QueryException if the query contains null and an operator that does not permit nulls
     */
    private void append(BinaryConditionalOperator operator) throws QueryException {
        ValueNode value = operator.getValueNode();
        Selector type = operator.getType();
        if (!value.isNull()) {
            whereBuilder.append(" ");
            whereBuilder.append(!negative ? getSQLOperator(type) : getNegatedSQLOperator(type));
            whereBuilder.append(" ");
            value.traverse(this);
        } else {
            if (!Selector.EQUALS.equals(type) && !Selector.NEQUALS.equals(type)) {
                throw new QueryException("CMEBADQUERY", "400", "This query is malformed.");
            }
            whereBuilder.append(" IS ");
            if((Selector.EQUALS.equals(type) && negative)
                    || (Selector.NEQUALS.equals(type) && !negative)) {
                whereBuilder.append("NOT ");
            }
            whereBuilder.append("NULL");       
        }
    }

    /**
     * Special logic for the IN operator
     * @param operator
     * @throws QueryException
     */
    private void append(NAryConditionalOperator operator) throws QueryException {
        if (!Selector.IN.equals(operator.getType()) && !Selector.NOT_IN.equals(operator.getType())) throw new UnsupportedOperationException();
        whereBuilder.append(" ");
        if ((Selector.NOT_IN.equals(operator.getType()) && !negative)
                || (negative && Selector.IN.equals(operator.getType()))) {
            whereBuilder.append("NOT ");
        }
        whereBuilder.append("IN(");
        buildInParams(operator);
        whereBuilder.append(")"); 
    }

    /**
     * Special logic for the LIKE operator
     * @param operator
     * @throws QueryException
     */
    private void append(WildcardConditionalOperator operator) throws QueryException {
        Selector type = operator.getType();
        if (!Selector.LIKE.equals(type)) throw new UnsupportedOperationException();
        if (negative) {
            whereBuilder.append(" NOT");
        }
        whereBuilder.append(" LIKE ? ESCAPE '!'");
        WildcardValueNode wildcardValue = buildSQLWildcardValue(operator.getValueNode(), operator.getMatchType());
        wildcardValue.traverse(this);
    }
    
    private void append(GroupMatchOperator operator) throws QueryException {
        Selector type = operator.getType();
        if (!Selector.ELEMMATCH.equals(type)) throw new UnsupportedOperationException();
        Query groupQuery = operator.getFilter();
        groupQuery.getOperator().traverse(this);
    }

    /**
     * Adds a parameter to the WHERE clause for use in a PreparedStatement
     */
    @Override
    public void append(ValueNode node) throws QueryException {
        if (!(node instanceof WildcardValueNode)) {
            whereBuilder.append("?");
        }
        parameters.add(node);
    }

    private void addSpace() {
        if(whereBuilder.length() > 0) {
            whereBuilder.append(" ");
        }
    }

    private void appendColumn(String fieldName) throws QueryException {
        String currentParent = null;
        String [] columnParts = fieldName.split("\\.");
        String columnName = columnParts[columnParts.length - 1];
        for (int iii = 0; iii < columnParts.length - 1; iii++) {
            String previousParent = currentParent;
            currentParent = columnParts[iii];
            if (childJoins.get(currentParent) == null) {
                buildJoin(previousParent, currentParent);
            }
        }
        appendColumn(currentParent, columnName);
    }
    
    private void buildJoin(String parent, String child) throws QueryException {
        CMEObjectDef parentDef = null;
        CMEObjectDef childDef = null;
        if (parent == null) {
            parentDef = this.objDef;
        } else {
            Join parentJoin = childJoins.get(parent);
            if (parentJoin != null) {
                parentDef = parentJoin.getObject();
            }
        }
        if (parentDef == null) {
            throw new QueryException("CMEQUERYERR", "500", "Parent object definition not found: " + parent);
        }
        List<CMEObjectChildDef> children = parentDef.getChildDefs();
        List<CMEChildRelKeysDef> relKeys = null;
        CMEObjectChildDef childObjDef = findChildByRefCol(child, children);
        if (childObjDef == null) {
            throw new QueryException("CMEBADQUERY", "400",
                    "Child reference [" + child + "] not found for parent object [" + parentDef.getName() + "].");
        }
        relKeys = childObjDef.getKeys();
        childDef = CMEObjectDef.getCmeObjDef(childObjDef.getChildName());
        CMEViewDef parentView;
        CMEObjectViewRelationship parentViewRel;
        if (parent != null) { 
            parentViewRel = parentDef.getViews(ObjectAction.SEARCH).get(0);
            parentView = CMEViewDef.getViewDef(parentViewRel.getViewName());
        } else {
            parentViewRel = this.objViewRel;
            parentView = this.view;
        }
        CMEObjectViewRelationship childViewRel = childDef.getViews(ObjectAction.SEARCH).get(0);
        CMEViewDef childView = CMEViewDef.getViewDef(childViewRel.getViewName());
        List<ImmutablePair<String,String>> joinColumns = new ArrayList<>();
        String parentAlias = "";
        String childTable = "";
        for (CMEChildRelKeysDef key : relKeys) {
            String prevAlias = parentAlias;
            String prevTable = childTable;
            String parentColumnName = parentViewRel.getViewColName(key.getParentColumnName());
            String childColumnName = childViewRel.getViewColName(key.getColumnName());
            CMEViewColumnDef parentColDef = parentView.getColumnByName(parentColumnName);
            if (overrideAlias == null) {
                parentAlias = parentColDef.getTableAlias();
            } else {
                parentAlias = overrideAlias;
            }
            if (!prevAlias.equals("") && !parentAlias.equals(prevAlias)) {
                throw new UnsupportedOperationException("Can't build joins across multiple tables");
            }
            CMEViewColumnDef childColDef = childView.getColumnByName(childColumnName);
            childTable = childColDef.getTableName();
            if (!prevTable.equals("") && !childTable.equals(prevTable)) {
                throw new UnsupportedOperationException("Can't build joins across multiple tables");
            }
            joinColumns.add(new ImmutablePair<>(parentColumnName, childColumnName));            
        }
        if (parent != null) {
            parentAlias = childJoins.get(parent).getChildAlias();
        }
        Join join = new Join(childTable, parentAlias, JoinType.INNER, joinColumns, childView, childDef, childObjDef.getType());
        childJoins.put(child, join);
    }

    private CMEObjectChildDef findChildByRefCol(String name, List<CMEObjectChildDef> children) {
        CMEObjectChildDef child = null;
        for (CMEObjectChildDef candidate : children) {
            if(name.equals(candidate.getRefColName())) {
                child = candidate;
                break;
            }
        }
        return child;
    }

    private CMEViewColumnDef getViewColumn(CMEViewDef view, CMEObjectViewRelationship objViewRel, String objFieldName) throws QueryException {
        CMEViewColumnDef viewField = view.getColumnByName(objViewRel.getViewColName(objFieldName));
        if (null == viewField) {
            throw new QueryException("CMEOBJNOSUCHFIELD", "400", "No such field: " + objFieldName);
        }
        return viewField;
    }

    /**
     * Adds a fully qualified column to the WHERE clause 
     * @param viewField the CMEViewColumnDef from the view being queried
     */
    private void appendColumn(String parent, String field) throws QueryException {
        String alias = "";
        String column = "";
        if (parent == null) {
            CMEViewColumnDef viewField = getViewColumn(view, objViewRel, field);
            if (overrideAlias == null) {
                alias = viewField.getTableAlias();
            } else {
                alias = overrideAlias;
            }
            column = viewField.getColumnName();
        } else {
            Join join = childJoins.get(parent);
            if (join == null) {
                throw new QueryException("CMEQUERYERR", "500", "Unable to find join definition for parent: " + parent);
            }
            alias = join.getChildAlias();
            CMEViewDef view = join.getView();
            column = view.getColumnByName(field).getColumnName();            
        }
        addSpace();
        if (alias.length() > 0) {
            whereBuilder.append(alias);
            whereBuilder.append(".");
        }
        whereBuilder.append(column);
    }

    private String getSQLOperator(Selector selector) throws QueryException {
        String operator;
        switch(selector) {
        case EQUALS:
            operator = "=";
            break;
        case NEQUALS:
            operator = "<>";
            break;
        case GT:
            operator = ">";
            break;
        case GTE:
            operator = ">=";
            break;
        case LT:
            operator = "<";
            break;
        case LTE:
            operator = "<=";
            break;
        case MODULUS:
            throw new UnsupportedOperationException();
        default:
            throw new QueryException("CMEBADQUERY", "400", "Invalid operator in query.");            
        }
        return operator;
    }

    private String getNegatedSQLOperator(Selector selector) throws QueryException {
        String operator;
        switch(selector) {
        case EQUALS:
            operator = "!=";
            break;
        case NEQUALS:
            operator = "=";
            break;
        case GT:
            operator = "<=";
            break;
        case GTE:
            operator = "<";
            break;
        case LT:
            operator = ">=";
            break;
        case LTE:
            operator = ">";
            break;
        case MODULUS:
            operator = "%";
            break;
        default:
            throw new QueryException("CMEBADQUERY", "400", "Invalid operator in query.");            
        }
        return operator;
    }

    private String getSQLBoolean(Combinator combinator) throws QueryException {
        String operator;
        switch (combinator) {
        case AND:
        case NAND:
            operator = "AND";
            break;
        case OR:
        case NOR:
            operator = "OR";
            break;
        case NOT:
        default:
            throw new QueryException("CMEBADQUERY", "400", "This query is malformed.");                
        }
        return operator;
    }

    /**
     * Builds out the comma separated list of parameters for an IN clause
     * @param operator the IN operator
     * @throws QueryException if the operation would generate illegal SQL (am empty IN clause)
     */
    private void buildInParams(NAryConditionalOperator operator) throws QueryException {
        boolean first = true;
        if (operator.getValues().size() == 0) throw new QueryException("CMEBADQUERY", "400", "This query in malformed.");
        for (ValueNode valueNode : operator.getValues()) {
            if (!first) {
                whereBuilder.append(", ");
            }
            valueNode.traverse(this);
            first = false;
        }
    }

    private WildcardValueNode buildSQLWildcardValue(ValueNode value, MatchType matcher) {
        return new WildcardValueNode(value, matcher);        
    }

    private void merge(SQLQueryBuilder builder) {
        whereBuilder.append(builder.getWhereBuilder());
        parameters.addAll(builder.getParameters());
        childJoins.putAll(builder.getChildJoins());
    }

    /**
     * A private class that encapsulates SQL wildcard values used in LIKE operations.
     * The getText method will escape wildcard characters in the user supplied
     * String and add the appropriate % characters for the match type
     */
    private class WildcardValueNode implements ValueNode {
        private ValueNode value;
        private MatchType matcher;

        public WildcardValueNode(ValueNode value, MatchType matcher) {
            this.value = value;
            this.matcher = matcher;
        }

        @Override
        public void traverse(QueryBuilder builder) throws QueryException {
            builder.append(this);
        }

        @Override
        public ValueType getValueType() {
            return value.getValueType();
        }

        @Override
        public boolean isNull() {
            return value.isNull();
        }

        @Override
        public Boolean getBoolean() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Date getDate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Number getNumber() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getText() {
            String matchText;
            String matchValue = escapeWildcards(value.getText());
            switch(matcher) {
            case BEGINS:
                matchText = matchValue + "%";
                break;
            case CONTAINS:
                matchText = "%" + matchValue + "%";
                break;
            case ENDS:
                matchText = "%" + matchValue;
                break;
            default:
                throw new UnsupportedOperationException();
            }
            return matchText;
        }

        @Override
        public Time getTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Timestamp getTimestamp() {
            throw new UnsupportedOperationException();
        }

        @Override
        public UUID getUUID() {
            throw new UnsupportedOperationException();
        }

        private String escapeWildcards(String value) {
            return value.replace("%", "!%")
                    .replace("!", "!!")
                    .replace("_", "!_")
                    .replace("[", "![");
        }
    }
    
    public static class Join {
        private String childTableName;
        private String childAlias;
        private String parentAlias;
        private JoinType joinType;
        private List<ImmutablePair<String, String>> joinColumns;
        private CMEViewDef view;
        private CMEObjectDef objectDef;
        private CMEObjectType logicalType;
        private static final Random repeatablePRNG = new Random(2501);
        public Join(String childTableName, String parentAlias, JoinType joinType, List<ImmutablePair<String, String>> joinColumns, CMEViewDef view, CMEObjectDef objectDef, CMEObjectType logicalType) {
            this.childTableName = childTableName;
            this.parentAlias = parentAlias;
            this.joinType = joinType;
            this.joinColumns = joinColumns;
            childAlias = generateChildAlias();
            this.view = view;
            this.objectDef = objectDef;
            this.logicalType = logicalType;
        }
        
        public String getChildAlias() {
            return childAlias;
        }
        
        public CMEViewDef getView() {
            return view;
        }

        public CMEObjectDef getObject() {
            return objectDef;
        }
        
        public CMEObjectType getLogicalType() {
            return logicalType;
        }
        
        public static void resetRandomizer() {
            repeatablePRNG.setSeed(2501);
        }
        
        public StringBuilder build() {
            StringBuilder sb = new StringBuilder();
            sb.append(getJoinKeyword());
            sb.append(" ");
            sb.append(childTableName);
            sb.append(" ");
            sb.append(childAlias);
            sb.append(" ON ");
            boolean first = true;
            for (ImmutablePair<String, String> pair : joinColumns) {
                if (!first) {
                    sb.append(" AND ");
                }
                sb.append(parentAlias);
                sb.append(".");
                sb.append(pair.left);
                sb.append(" = ");
                sb.append(childAlias);
                sb.append(".");
                sb.append(pair.right);
                first = false;
            }
            return sb;
        }
        
        private String getJoinKeyword() {
            switch(joinType) {
                case LEFT:
                    return "LEFT OUTER JOIN";
                case RIGHT:
                    return "RIGHT OUTER JOIN";
                case FULL:
                    return "FULL OUTER JOIN";
                case INNER:
                default:
                    return "INNER JOIN";
            }
        }

        private String generateChildAlias() {
            StringBuilder aliasBuilder = new StringBuilder();
            String baseName = childTableName.replace("t_ccs_", "");
            Pattern pattern = Pattern.compile("(_[a-z0-9])");
            Matcher matcher = pattern.matcher(baseName);
            aliasBuilder.append(baseName.substring(0, 1));
            while (matcher.find()) {
                String next = matcher.group(1);
                if (next != null && next.length() > 0) {
                    aliasBuilder.append(next.substring(1));
                }
            }
            // Add a random number to reduce chances of a conflict
            aliasBuilder.append((int)Math.floor(repeatablePRNG.nextDouble() * 9999));
            return aliasBuilder.toString();
        }
    }
    
}
