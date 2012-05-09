package org.naw.tasks.factory.xml;

import org.naw.tasks.Merge;
import org.w3c.dom.Element;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;

public class MergeDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "merge";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Merge.class;
	}

	protected void doParse(Element element,
			ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {

		builder.setObjectQName(element.getAttribute("name"));

		builder.addPropertyValue("from", element.getAttribute("from"));
		builder.addPropertyValue("to", element.getAttribute("to"));
	}
}
