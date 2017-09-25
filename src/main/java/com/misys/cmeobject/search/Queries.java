package com.misys.cmeobject.search;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Contains a collection of interfaces that define a generic query syntax tree for API searches.
 */
public class Queries {
    /**
     * Represents an abstract query for information.
     */
    public static interface Query {
        /**
         * Gets the operator this Query will perform.
         * @return The operator this Query will perform.
         */
        public QueryNode getOperator();
    }
    
    /**
     * Represents a node in a Query's abstract syntax tree.
     */
    public static interface QueryNode {
        /**
         * Traverse this QueryNode with the given QueryBuilder.
         * @param builder The QueryBuilder to use to traverse this node.
         * @throws QueryException if this node cannot be traversed because there's an issue with the Query.
         */
        public void traverse(QueryBuilder builder) throws QueryException;
    }

    /**
     * Type of a Selector operator.
     */
    public enum Selector {
        EQUALS,
        NEQUALS,
        MODULUS,
        GT,
        GTE,
        LT,
        LTE,
        IN,
        NOT_IN,
        LIKE,
        ELEMMATCH;
    }

    public static interface SelectorNode extends QueryNode {
        public String getFieldName();
        public ConditionalOperator getOperator();
        default void traverse(QueryBuilder builder) throws QueryException {
            builder.append(this);
        }
    }

    public static interface ConditionalOperator {
        public Selector getType();
    }

    public static interface BinaryConditionalOperator extends ConditionalOperator {
        public ValueNode getValueNode();
    }

    public static interface NAryConditionalOperator extends ConditionalOperator {
        public List<? extends ValueNode> getValues();
    }
    
    public static interface GroupMatchOperator extends ConditionalOperator {
        public Query getFilter();
    }

    public enum MatchType {
        BEGINS,
        CONTAINS,
        ENDS;
    }

    public static interface WildcardConditionalOperator extends BinaryConditionalOperator {
        public MatchType getMatchType();
    }

    public enum Combinator {
        AND,
        OR,
        NAND,
        NOR,
        NOT;
    }

    public static interface CombinatorNode extends QueryNode {
        public List<Query> getChildren();
        public Combinator getCombinatorType();
        default void traverse(QueryBuilder builder) throws QueryException {
            builder.append(this);
        }
    }

    public enum ValueType {
        BOOLEAN,
        DATE,
        NULL,
        NUMBER,
        TEXT,
        TIME,
        TIMESTAMP,
        UUID
    }

    public static interface ValueNode extends QueryNode {
        public ValueType getValueType();
        public boolean isNull();
        public Boolean getBoolean();
        public Date getDate();
        public Number getNumber();
        public String getText();
        public Time getTime();
        public Timestamp getTimestamp();
        public UUID getUUID();
        default void traverse(QueryBuilder builder) throws QueryException {
            builder.append(this);
        }
    }
}
