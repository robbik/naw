package org.naw.tasks.factory.xml;

import java.util.ArrayList;
import java.util.List;

import org.naw.tasks.Fork;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;

public class ForkDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "fork";

	public static final String ELEMENT_FLOW_LOCAL_NAME = "flow";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Fork.class;
	}

	protected void doParse(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		List<Object> flowTasks = new ArrayList<Object>();
		
		NodeList childNodes = element.getChildNodes();
		
		for (int i = 0, n = childNodes.getLength(); i < n; ++i) {
			Node node = childNodes.item(i);
			
			if (node instanceof Element) {
				if (ELEMENT_FLOW_LOCAL_NAME.equals(delegate.getLocalName(node))) {
					flowTasks.add(delegate.parseChildElements((Element) node));
				}
			}
		}
		
		builder.setObjectQName(element.getAttribute("name"));
		builder.addPropertyValue("flowTasks", flowTasks.toArray());
		
		flowTasks.clear();
		flowTasks = null;
	}
}
