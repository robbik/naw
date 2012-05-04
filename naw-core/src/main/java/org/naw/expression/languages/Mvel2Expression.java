package org.naw.expression.languages;

import java.util.HashMap;
import java.util.Map;

import org.mvel2.MVEL;
import org.naw.core.task.DataExchange;
import org.naw.expression.Expression;

public class Mvel2Expression extends Expression {
	
	private final Object compiled;
	
	public Mvel2Expression(Object compiled) {
		this.compiled = compiled;
	}

	public Object eval(DataExchange exchange) throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		root.put("exchange", exchange);
		root.put("objects", objects);
		
		return MVEL.executeExpression(compiled, root);
	}

	public <T> T eval(DataExchange exchange, Class<? extends T> returnType) throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		root.put("exchange", exchange);
		root.put("objects", objects);
		
		return (T) MVEL.executeExpression(compiled, root, returnType);
	}
}
