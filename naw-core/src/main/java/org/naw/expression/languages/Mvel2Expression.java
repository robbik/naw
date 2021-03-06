package org.naw.expression.languages;

import java.util.HashMap;
import java.util.Map;

import org.mvel2.MVEL;
import org.naw.core.exchange.MessageExchange;
import org.naw.expression.Expression;

public class Mvel2Expression extends Expression {
	
	private final String str;
	
	private final Object compiled;
	
	public Mvel2Expression(String str, Object compiled) {
		this.str = str;
		this.compiled = compiled;
	}

	public Object eval(MessageExchange exchange) throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		root.put("exchange", exchange);
		root.put("objects", objects);
		
		return MVEL.executeExpression(compiled, root);
	}

	public <T> T eval(MessageExchange exchange, Class<? extends T> returnType) throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		root.put("exchange", exchange);
		root.put("objects", objects);
		
		return (T) MVEL.executeExpression(compiled, root, returnType);
	}
	
	@Override
	public String toString() {
		return Mvel2Expression.class + " [ expression: " + str + " ]";
	}
}
