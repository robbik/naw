package org.naw.expression.languages;

import org.mvel2.MVEL;
import org.naw.expression.Expression;
import org.naw.expression.ExpressionHandler;

public class Mvel2ExpressionHandler implements ExpressionHandler {
	
	public Expression parse(String expression) throws Exception {
		return new Mvel2Expression(expression, MVEL.compileExpression(expression));
	}
}
