package org.naw.tasks.factory.xml;

import java.util.ArrayList;
import java.util.List;

import org.naw.tasks.If;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;

public class IfDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "if";

	public static final String ELEMENT_ELSE_LOCAL_NAME = "else";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return If.class;
	}

	protected void doParse(Element element,
			ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {

		builder.setObjectName(element.getAttribute("name"));

		builder.addPropertyValue("predicate", delegate.parseFirstChildElement(element));

		List<Object> thenTasks = new ArrayList<Object>();
		List<Object> elseTasks = new ArrayList<Object>();
		
		boolean elseMode = false;
		
		NodeList childNodes = element.getChildNodes();

		for (int i = 0, j = 0, n = childNodes.getLength(); i < n; ++i) {
			Node node = childNodes.item(i);

			if (node instanceof Element) {
				if (j > 0) {
					if (ELEMENT_ELSE_LOCAL_NAME.equals(delegate.getLocalName(node))) {
						elseMode = true;
					} else {
						if (elseMode) {
							elseTasks.add(delegate.parse((Element) node));
						} else {
							thenTasks.add(delegate.parse((Element) node));
						}
					}
				}
				
				++j;
			}
		}

		builder.addPropertyValue("thenTasks", thenTasks);
		
		if (!elseTasks.isEmpty()) {
			builder.addPropertyValue("elseTasks", elseTasks);
		}
	}
}
