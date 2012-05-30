package org.naw.tasks.factory.xml;

import org.naw.tasks.Send;
import org.w3c.dom.Element;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;
import rk.commons.util.StringHelper;

public class SendDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "send";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Send.class;
	}

	protected void doParse(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		builder.setObjectQName(element.getAttribute("name"));

		builder.addPropertyValue("variable", element.getAttribute("variable"));
		builder.addPropertyValue("retriable", element.getAttribute("retriable"));

		builder.addPropertyValue("link", element.getAttribute("to"));

		String stmp = element.getAttribute("exchangeVariable");

		if (StringHelper.hasText(stmp)) {
			builder.addPropertyValue("exchangeVariable", stmp);
		}
	}
}
