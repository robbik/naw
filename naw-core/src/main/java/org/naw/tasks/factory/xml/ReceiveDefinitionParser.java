package org.naw.tasks.factory.xml;

import org.naw.tasks.Receive;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;
import rk.commons.util.StringHelper;

public class ReceiveDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "receive";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Receive.class;
	}
	
	private void parseReceived(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		builder.addPropertyValue("variable", element.getAttribute("variable"));
		builder.addPropertyValue("receiveTasks", delegate.parseChildElements(element));
	}
	
	private void parseTimeout(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		int deadlineDuration = 0;
		
		if (StringHelper.hasText(element.getAttribute("deadline"))) {
			builder.addPropertyValue("deadline", element.getAttribute("deadline"));
			++deadlineDuration;
		}
		
		if (StringHelper.hasText(element.getAttribute("duration"))) {
			builder.addPropertyValue("duration", element.getAttribute("duration"));
			++deadlineDuration;
		}
		
		if (deadlineDuration == 0) {
			throw new IllegalArgumentException("deadline or duration until must be specified");
		} else if (deadlineDuration > 1) {
			throw new IllegalArgumentException("duration cannot be appeared if deadline is specified, and vice versa");
		}
		
		builder.addPropertyValue("timeoutTasks", delegate.parseChildElements(element));
	}

	protected void doParse(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		Element receivedNode = null, errorNode = null, timeoutNode = null;
		
		NodeList childNodes = element.getChildNodes();
		
		for (int i = 0, n = childNodes.getLength(); i < n; ++i) {
			Node childNode = childNodes.item(i);
			
			if (childNode instanceof Element) {
				if ("received".equals(delegate.getLocalName(childNode))) {
					receivedNode = (Element) childNode;
				} else if ("error".equals(delegate.getLocalName(childNode))) {
					errorNode = (Element) childNode;
				} else if ("timeout".equals(delegate.getLocalName(childNode))) {
					timeoutNode = (Element) childNode;
				}
			}
		}
		
		boolean createInstance = false;
		
		builder.setObjectName(element.getAttribute("name"));
		
		builder.addPropertyValue("link", element.getAttribute("from"));

		String stmp = element.getAttribute("createInstance");
		if (StringHelper.hasText(stmp)) {
			createInstance = Boolean.parseBoolean(stmp);
			
			builder.addPropertyValue("createInstance", stmp);
		}
		
		stmp = element.getAttribute("exchangeVariable");
		
		if (StringHelper.hasText(stmp)) {
			builder.addPropertyValue("exchangeVariable", stmp);
		}
		
		parseReceived(receivedNode, delegate, builder);
		
		if (errorNode != null) {
			if (createInstance) {
				throw new IllegalArgumentException("error must not be specified if createInstance is true");
			}
			
			builder.addPropertyValue("errorTasks", delegate.parseChildElements(errorNode));
		}
		
		if (timeoutNode != null) {
			if (createInstance) {
				throw new IllegalArgumentException("timeout must not be specified if createInstance is true");
			}
			
			parseTimeout(timeoutNode, delegate, builder);
		}
	}
}
