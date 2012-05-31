package org.naw.expression.languages;

import org.naw.expression.Expression;
import org.naw.expression.ExpressionHandler;
import org.springframework.context.expression.EnvironmentAccessor;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpelExpressionHandler implements ExpressionHandler {
	
	public Expression parse(String expression) throws Exception {
		SpelExpressionParser parser = new SpelExpressionParser();
		
		org.springframework.expression.Expression exp = parser.parseExpression(expression);
		
		StandardEvaluationContext evalctx = new StandardEvaluationContext();
		evalctx.addPropertyAccessor(new MapAccessor());
		evalctx.addPropertyAccessor(new EnvironmentAccessor());

		return new SpelExpression(exp, evalctx);
	}
}
