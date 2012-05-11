package org.naw.tasks.factory.xml;

import org.naw.tasks.Correlation;
import org.w3c.dom.Element;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;

public class CorrelationDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "correlation";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Correlation.class;
	}

	protected void doParse(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		builder.setObjectQName(element.getAttribute("name"));

		builder.addPropertyValue("exchangeVariable", element.getAttribute("exchangeVariable"));
		
		builder.addPropertyValue("value", delegate.parseFirstChildElement(element));
	}
}
