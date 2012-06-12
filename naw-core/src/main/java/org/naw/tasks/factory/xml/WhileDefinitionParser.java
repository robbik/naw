package org.naw.tasks.factory.xml;

import java.util.List;

import org.naw.tasks.While;
import org.w3c.dom.Element;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;

public class WhileDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "while";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return While.class;
	}

	protected void doParse(Element element,
			ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {

		builder.setObjectName(element.getAttribute("name"));
		
		List<Object> childObjects = delegate.parseChildElements(element);
		
		builder.addPropertyValue("predicate", childObjects.get(0));
		
		childObjects.remove(0);
		
		builder.addPropertyValue("bodyTasks", childObjects);
	}
}
