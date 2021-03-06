package org.naw.expression.languages;

import java.util.HashMap;
import java.util.Map;

import ognl.Ognl;

import org.naw.core.exchange.MessageExchange;
import org.naw.expression.Expression;

public class OgnlExpression extends Expression {
	
	private final Object tree;
	
	public OgnlExpression(Object tree) {
		this.tree = tree;
	}

	public Object eval(MessageExchange exchange) throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		root.put("exchange", exchange);
		root.put("objects", objects);
		
		return Ognl.getValue(tree, root);
	}

	@SuppressWarnings("unchecked")
	public <T> T eval(MessageExchange exchange, Class<? extends T> returnType) throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		root.put("exchange", exchange);
		root.put("objects", objects);
		
		return (T) Ognl.getValue(tree, (Object) root, returnType);
	}
	
	@Override
	public String toString() {
		return OgnlExpression.class + " [ expression: " + tree + " ]";
	}
}
