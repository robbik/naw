package org.naw.tasks.factory.xml;

import org.naw.tasks.Reply;
import org.w3c.dom.Element;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;
import rk.commons.util.StringHelper;

public class ReplyDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "reply";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Reply.class;
	}

	protected void doParse(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		builder.setObjectName(element.getAttribute("name"));
		
		builder.addPropertyValue("variable", element.getAttribute("variable"));
		builder.addPropertyValue("retriable", element.getAttribute("retriable"));
		
		builder.addPropertyValue("exchangeVariable", element.getAttribute("exchangeVariable"));
		
		String stmp = element.getAttribute("to");
		if (StringHelper.hasText(stmp)) {
			builder.addPropertyValue("to", stmp);
		}
	}
}
