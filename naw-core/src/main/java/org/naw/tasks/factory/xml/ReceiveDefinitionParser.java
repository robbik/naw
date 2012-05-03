package org.naw.tasks.factory.xml;

import java.util.ArrayList;
import java.util.List;

import org.naw.tasks.Receive;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rk.commons.ioc.factory.support.ObjectDefinitionBuilder;
import rk.commons.ioc.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.ioc.factory.xml.SingleObjectDefinitionParser;
import rk.commons.util.StringUtils;

public class ReceiveDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "receive";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Receive.class;
	}
	
	private void parseReceived(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		List<Object> tasks = new ArrayList<Object>();
		
		NodeList childNodes = element.getChildNodes();
		
		for (int i = 0, n = childNodes.getLength(); i < n; ++i) {
			Node childNode = childNodes.item(i);
			
			if (childNode instanceof Element) {
				tasks.add(delegate.parse((Element) childNode));
			}
		}

		builder.addPropertyValue("varName", element.getAttribute("varName"));
		builder.addPropertyValue("onReceiveTasks", tasks);
	}
	
	private void parseTimeout(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		int deadlineDuration = 0;
		
		if (StringUtils.hasText(element.getAttribute("deadline"))) {
			builder.addPropertyValue("deadline", element.getAttribute("deadline"));
			++deadlineDuration;
		}
		
		if (StringUtils.hasText(element.getAttribute("duration"))) {
			builder.addPropertyValue("duration", element.getAttribute("duration"));
			++deadlineDuration;
		}
		
		if (deadlineDuration == 0) {
			throw new IllegalArgumentException("deadline or duration until must be specified");
		} else if (deadlineDuration > 1) {
			throw new IllegalArgumentException("duration cannot be appeared if deadline is specified, and vice versa");
		}
		
		List<Object> tasks = new ArrayList<Object>();
		
		NodeList childNodes = element.getChildNodes();
		
		for (int i = 0, n = childNodes.getLength(); i < n; ++i) {
			Node childNode = childNodes.item(i);
			
			if (childNode instanceof Element) {
				tasks.add(delegate.parse((Element) childNode));
			}
		}

		builder.addPropertyValue("onTimeoutTasks", tasks);
	}

	protected void doParse(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		Element receivedNode = null, timeoutNode = null;
		
		NodeList childNodes = element.getChildNodes();
		
		for (int i = 0, n = childNodes.getLength(); i < n; ++i) {
			Node childNode = childNodes.item(i);
			
			if (childNode instanceof Element) {
				if ("received".equals(delegate.getLocalName(childNode))) {
					receivedNode = (Element) childNode;
				} else if ("timeout".equals(delegate.getLocalName(childNode))) {
					timeoutNode = (Element) childNode;
				}
			}
		}
		
		boolean entryPoint = false;
		
		builder.setObjectQName(element.getAttribute("name"));
		
		builder.addPropertyValue("partnerLink", element.getAttribute("from"));

		String tmpstr = element.getAttribute("entryPoint");
		if (StringUtils.hasText(tmpstr)) {
			entryPoint = Boolean.parseBoolean(tmpstr);
			
			builder.addPropertyValue("entryPoint", tmpstr);
		}
		
		String sessionId = element.getAttribute("exchange");
		
		if (StringUtils.hasText(sessionId)) {
			if (entryPoint) {
				throw new IllegalArgumentException("exchange must not be specified if entryPoint is true");
			}

			builder.addPropertyValue("exchangeId", sessionId);
		}
		
		parseReceived(receivedNode, delegate, builder);
		
		if (timeoutNode != null) {
			if (entryPoint) {
				throw new IllegalArgumentException("timeout must no be specified if entryPoint is true");
			}
			
			parseTimeout(timeoutNode, delegate, builder);
		}
	}
}
