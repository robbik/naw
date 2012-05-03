package org.naw.expression.languages;

import ognl.Ognl;

import org.naw.core.task.DataExchange;
import org.naw.expression.Expression;

public class OgnlExpression extends Expression {
	
	private final Object tree;
	
	public OgnlExpression(Object tree) {
		this.tree = tree;
	}

	public void execute(DataExchange exchange) throws Exception {
		Ognl.getValue(tree, exchange);
	}

	public boolean getBoolean(DataExchange exchange) throws Exception {
		return Boolean.TRUE.equals(Ognl.getValue(tree, (Object) exchange, Boolean.class));
	}
}
