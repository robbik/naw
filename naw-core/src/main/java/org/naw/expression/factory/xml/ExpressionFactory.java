package org.naw.expression.factory.xml;

import org.naw.expression.Expression;
import org.naw.expression.ExpressionHandlerResolver;

import rk.commons.inject.factory.IocObjectFactory;
import rk.commons.inject.factory.support.IocObjectFactoryAware;
import rk.commons.inject.factory.support.ObjectFactory;

public class ExpressionFactory extends ObjectFactory<Expression> implements IocObjectFactoryAware {

	private ExpressionHandlerResolver resolver;

	private String language;

	private String expression;
	
	private IocObjectFactory iocObjectFactory;

	public void setResolver(ExpressionHandlerResolver resolver) {
		this.resolver = resolver;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	public void setIocObjectFactory(IocObjectFactory iocObjectFactory) {
		this.iocObjectFactory = iocObjectFactory;
	}

	protected Expression createInstance() {
		Expression compiled;
		
		try {
			compiled = resolver.parse(language, expression);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse expression '"
					+ expression + "' for language " + language, e);
		}
		
		compiled.setIocObjectFactory(iocObjectFactory);
		
		return compiled;
	}
}
