package org.naw.expression.factory.xml;

import org.naw.expression.Expression;
import org.naw.expression.ExpressionHandlerResolver;

import rk.commons.inject.annotation.Inject;
import rk.commons.inject.factory.ObjectFactory;
import rk.commons.inject.factory.support.FactoryObject;

public class ExpressionFactory extends FactoryObject<Expression> {

	private ExpressionHandlerResolver resolver;

	private String language;

	private String expression;
	
	@Inject
	private ObjectFactory factory;
	
	@Inject
	private String objectName;

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
		Expression compiled;
		
		try {
			compiled = resolver.parse(language, expression);
		} catch (Exception e) {
			throw new IllegalArgumentException("unable to parse expression '"
					+ expression + "' for language " + language, e);
		}
		
		compiled.setId(objectName);
		compiled.setObjectFactory(factory);
		
		return compiled;
	}
}
