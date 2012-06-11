package org.naw.expression.factory.xml;

import org.naw.expression.Expression;
import org.naw.expression.ExpressionHandlerResolver;

import rk.commons.inject.factory.ObjectFactory;
import rk.commons.inject.factory.support.FactoryObject;
import rk.commons.inject.factory.support.ObjectFactoryAware;
import rk.commons.inject.factory.support.ObjectQNameAware;

public class ExpressionFactory extends FactoryObject<Expression> implements ObjectFactoryAware, ObjectQNameAware {

	private ExpressionHandlerResolver resolver;

	private String language;

	private String expression;
	
	private ObjectFactory factory;
	
	private String objectQName;

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

	public void setObjectQName(String objectQName) {
		this.objectQName = objectQName;
	}

	protected Expression createInstance() {
		Expression compiled;
		
		try {
			compiled = resolver.parse(language, expression);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse expression '"
					+ expression + "' for language " + language, e);
		}
		
		compiled.setObjectQName(objectQName);
		compiled.setObjectFactory(factory);
		
		return compiled;
	}
}
