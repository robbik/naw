package org.naw.tasks.factory.xml;

import org.naw.tasks.Empty;
import org.w3c.dom.Element;

import rk.commons.ioc.factory.support.ObjectDefinitionBuilder;
import rk.commons.ioc.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.ioc.factory.xml.SingleObjectDefinitionParser;

public class EmptyDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "noop";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Empty.class;
	}

	protected void doParse(Element element,
			ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {

		builder.setObjectQName(element.getAttribute("name"));
	}
}
