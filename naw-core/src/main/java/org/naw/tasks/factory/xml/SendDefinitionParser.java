package org.naw.tasks.factory.xml;

import org.naw.tasks.Send;
import org.w3c.dom.Element;

import rk.commons.ioc.factory.support.ObjectDefinitionBuilder;
import rk.commons.ioc.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.ioc.factory.xml.SingleObjectDefinitionParser;
import rk.commons.util.StringUtils;

public class SendDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "send";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Send.class;
	}

	protected void doParse(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		builder.setObjectQName(element.getAttribute("name"));
		
		builder.addPropertyValue("varName", element.getAttribute("varName"));
		builder.addPropertyValue("retriable", element.getAttribute("retriable"));

		builder.addPropertyValue("partnerLink", element.getAttribute("to"));
		
		String stmp = element.getAttribute("exchange");

		if (StringUtils.hasText(stmp)) {
			builder.addPropertyValue("exchangeId", stmp);
		}
	}
}
