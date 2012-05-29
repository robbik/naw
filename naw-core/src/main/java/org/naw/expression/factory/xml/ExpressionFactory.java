package org.naw.expression.factory.xml;

import org.naw.expression.Expression;
import org.naw.expression.ExpressionHandlerResolver;

import rk.commons.inject.factory.ObjectFactory;
import rk.commons.inject.factory.support.FactoryObject;
import rk.commons.inject.factory.support.ObjectFactoryAware;

public class ExpressionFactory extends FactoryObject<Expression> implements ObjectFactoryAware {

	private ExpressionHandlerResolver resolver;

	private String language;

	private String expression;
	
	private ObjectFactory factory;

	public void setResolver(ExpressionHandlerResolver resolver) {
		this.resolver = resolver;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	public void setObjectFactory(ObjectFactory factory) {
		this.factory = factory;
	}

	protected Expression createInstance() {
		Expression compiled;
		
		try {
			compiled = resolver.parse(language, expression);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse expression '"
					+ expression + "' for language " + language, e);
		}
		
		compiled.setObjectFactory(factory);
		
		return compiled;
	}
}
