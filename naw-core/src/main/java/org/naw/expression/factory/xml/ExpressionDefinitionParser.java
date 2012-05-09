package org.naw.expression.factory.xml;

import org.naw.expression.ExpressionHandlerResolver;
import org.w3c.dom.Element;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;
import rk.commons.util.StringUtils;

public class ExpressionDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "expression";
	
	private ExpressionHandlerResolver resolver;

	@Override
	protected Class<?> getObjectClass(Element element) {
		return ExpressionFactory.class;
	}

	protected void doParse(Element element,
			ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {

		if (resolver == null) {
			resolver = new ExpressionHandlerResolver(delegate.getResourceLoader());
		}
		
		String expression = element.getTextContent();
		if (!StringUtils.hasText(expression)) {
			throw new IllegalArgumentException("expression body must be set");
		}

		builder.setObjectQName(element.getAttribute("name"));
		
		builder.addPropertyValue("resolver", resolver);
		
		builder.addPropertyValue("language", element.getAttribute("language"));
		
		builder.addPropertyValue("expression", expression.trim());
	}
}
