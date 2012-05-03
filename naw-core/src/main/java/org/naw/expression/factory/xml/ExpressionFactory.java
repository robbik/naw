package org.naw.expression.factory.xml;

import org.naw.expression.Expression;
import org.naw.expression.ExpressionHandlerResolver;

import rk.commons.ioc.factory.support.ObjectFactory;

public class ExpressionFactory extends ObjectFactory<Expression> {

	private ExpressionHandlerResolver resolver;

	private String language;

	private String expression;

	public void setResolver(ExpressionHandlerResolver resolver) {
		this.resolver = resolver;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	protected Expression createInstance() {
		try {
			return resolver.parse(language, expression);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse expression '"
					+ expression + "' for language " + language, e);
		}
	}
}
