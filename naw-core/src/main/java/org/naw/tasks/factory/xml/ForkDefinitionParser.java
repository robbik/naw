package org.naw.tasks.factory.xml;

import java.util.ArrayList;
import java.util.List;

import org.naw.tasks.Fork;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rk.commons.ioc.factory.support.ObjectDefinitionBuilder;
import rk.commons.ioc.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.ioc.factory.xml.SingleObjectDefinitionParser;

public class ForkDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "fork";

	public static final String ELEMENT_FLOW_LOCAL_NAME = "flow";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Fork.class;
	}

	private List<Object> doParseFlow(Element element, ObjectDefinitionParserDelegate delegate) {
		List<Object> result = new ArrayList<Object>();
		
		NodeList childNodes = element.getChildNodes();
		
		for (int i = 0, n = childNodes.getLength(); i < n; ++i) {
			Node node = childNodes.item(i);
			
			if (node instanceof Element) {
				result.add(delegate.parse((Element) node));
			}
		}
		
		return result;
	}

	protected void doParse(Element element,
			ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {

		builder.setObjectQName(element.getAttribute("name"));

		List<Object> flowTasks = new ArrayList<Object>();
		
		NodeList childNodes = element.getChildNodes();
		
		for (int i = 0, n = childNodes.getLength(); i < n; ++i) {
			Node node = childNodes.item(i);
			
			if (node instanceof Element) {
				if (ELEMENT_FLOW_LOCAL_NAME.equals(delegate.getLocalName(node))) {
					flowTasks.add(doParseFlow((Element) node, delegate));
				}
			}
		}
		
		builder.addPropertyValue("flowTasks", flowTasks.toArray());
		
		flowTasks.clear();
		flowTasks = null;
	}
}
