package org.naw.tasks.factory.xml;

import org.naw.tasks.Log;
import org.w3c.dom.Element;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;

public class LogDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "log";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Log.class;
	}

	protected void doParse(Element element,
			ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {

		builder.setObjectQName(element.getAttribute("name"));
		
		builder.addPropertyValue("logName", element.getAttribute("logName"));
		builder.addPropertyValue("text", element.getAttribute("text"));
	}
}
