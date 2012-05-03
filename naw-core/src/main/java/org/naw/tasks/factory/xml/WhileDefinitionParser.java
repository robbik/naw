package org.naw.tasks.factory.xml;

import java.util.ArrayList;
import java.util.List;

import org.naw.tasks.While;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rk.commons.ioc.factory.support.ObjectDefinitionBuilder;
import rk.commons.ioc.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.ioc.factory.xml.SingleObjectDefinitionParser;

public class WhileDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "while";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return While.class;
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

		List<Object> bodyTasks = new ArrayList<Object>();

		++i;
		for (; i < length; ++i) {
			node = childNodes.item(i);

			if (node instanceof Element) {
				bodyTasks.add(delegate.parse((Element) node));
			}
		}

		builder.addPropertyValue("bodyTasks", bodyTasks);
	}
}
