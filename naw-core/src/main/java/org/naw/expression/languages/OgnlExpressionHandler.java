package org.naw.expression.languages;

import ognl.Ognl;
import ognl.OgnlContext;

import org.naw.expression.Expression;
import org.naw.expression.ExpressionHandler;

public class OgnlExpressionHandler implements ExpressionHandler {
	
	private static final Object EMPTY = new Object();

	public Expression parse(String expression) throws Exception {
		return new OgnlExpression(Ognl.compileExpression(new OgnlContext(), EMPTY, expression));
	}
}
