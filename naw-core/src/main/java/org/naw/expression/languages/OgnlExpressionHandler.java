package org.naw.expression.languages;

import ognl.Ognl;

import org.naw.expression.Expression;
import org.naw.expression.ExpressionHandler;

public class OgnlExpressionHandler implements ExpressionHandler {
	
	public Expression parse(String expression) throws Exception {
		return new OgnlExpression(Ognl.parseExpression(expression));
	}
}
