package com.misys.cmeobject.search;

import java.util.Map;

import com.misys.cmeobject.search.Queries.CombinatorNode;
import com.misys.cmeobject.search.Queries.ConditionalOperator;
import com.misys.cmeobject.search.Queries.SelectorNode;
import com.misys.cmeobject.search.Queries.ValueNode;

/**
 * Interface for classes that convert a generic query syntax tree into a query language.
 * Roughly based on the Visitor design pattern.
 */
public interface QueryBuilder {
    public void append(SelectorNode node) throws QueryException;
    public void append(CombinatorNode node) throws QueryException;
    public void append(ValueNode node) throws QueryException;
    public void append(ConditionalOperator operator) throws QueryException;
}