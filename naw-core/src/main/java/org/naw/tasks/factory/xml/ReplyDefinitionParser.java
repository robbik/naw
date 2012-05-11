package org.naw.tasks.factory.xml;

import org.naw.tasks.Reply;
import org.w3c.dom.Element;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;
import rk.commons.util.StringUtils;

public class ReplyDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "reply";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Reply.class;
	}

	protected void doParse(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		builder.setObjectQName(element.getAttribute("name"));
		
		builder.addPropertyValue("variable", element.getAttribute("variable"));
		builder.addPropertyValue("retriable", element.getAttribute("retriable"));
		
		String stmp = element.getAttribute("exchangeVariable");

		if (StringUtils.hasText(stmp)) {
			builder.addPropertyValue("exchangeVariable", stmp);
		}
	}
}
