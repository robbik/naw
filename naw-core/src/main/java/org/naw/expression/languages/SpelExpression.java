package org.naw.expression.languages;

import java.util.HashMap;
import java.util.Map;

import org.naw.core.exchange.MessageExchange;
import org.naw.expression.Expression;
import org.springframework.expression.EvaluationContext;

public class SpelExpression extends Expression {
	
	private final org.springframework.expression.Expression exp;
	
	private final EvaluationContext evalctx;
	
	public SpelExpression(org.springframework.expression.Expression exp, EvaluationContext evalctx) {
		this.exp = exp;
		this.evalctx = evalctx;
	}

	public Object eval(MessageExchange exchange) throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		root.put("exchange", exchange);
		root.put("objects", objects);
		
		return exp.getValue(evalctx, root);
	}

	public <T> T eval(MessageExchange exchange, Class<? extends T> returnType) throws Exception {
		Map<String, Object> root = new HashMap<String, Object>();
		
		root.put("exchange", exchange);
		root.put("objects", objects);
		
		return exp.getValue(evalctx, root, returnType);
	}
	
	@Override
	public String toString() {
		return SpelExpression.class + " [ expression: " + exp.getExpressionString() + " ]";
	}
}
