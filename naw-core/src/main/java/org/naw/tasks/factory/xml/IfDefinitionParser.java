package org.naw.tasks.factory.xml;

import java.util.ArrayList;
import java.util.List;

import org.naw.tasks.If;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rk.commons.ioc.factory.support.ObjectDefinitionBuilder;
import rk.commons.ioc.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.ioc.factory.xml.SingleObjectDefinitionParser;

public class IfDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "if";

	public static final String ELEMENT_ELSE_LOCAL_NAME = "else";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return If.class;
	}

	protected void doParse(Element element,
			ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {

		builder.setObjectQName(element.getAttribute("name"));

		NodeList childNodes = element.getChildNodes();

		int i = 0;
		int length = childNodes.getLength();

		Node node = null;

		while (i < length) {
			node = childNodes.item(i);

			if (node instanceof Element) {
				break;
			}

			++i;
		}

		builder.addPropertyValue("predicate", delegate.parse((Element) node));

		List<Object> thenTasks = new ArrayList<Object>();
		List<Object> elseTasks = new ArrayList<Object>();
		
		boolean elseMode = false;

		++i;
		for (; i < length; ++i) {
			node = childNodes.item(i);

			if (node instanceof Element) {
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
		}

		builder.addPropertyValue("thenTasks", thenTasks);
		
		if (!elseTasks.isEmpty()) {
			builder.addPropertyValue("elseTasks", elseTasks);
		}
	}
}
