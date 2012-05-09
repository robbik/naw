package org.naw.jndi.factory.xml;

import java.util.HashMap;
import java.util.Map;

import org.naw.jndi.factory.JndiContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;

public class JndiContextDefinitionParser extends SingleObjectDefinitionParser {
	
	public static final String ELEMENT_LOCAL_NAME = "jndi-context";
	
	@Override
	protected Class<?> getObjectClass(Element element) {
		return JndiContext.class;
	}

	protected void doParse(Element element,
			ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		
		builder.setObjectQName(element.getAttribute("name"));

		Map<String, Object> environment = new HashMap<String, Object>();
		
		NodeList nodes = element.getElementsByTagName("environment-property");
		
		for (int i = 0, n = nodes.getLength(); i < n; ++i) {
			Element prop = (Element) nodes.item(i);
			
			environment.put(prop.getAttribute("name"), prop.getAttribute("value"));
		}
		
		builder.addPropertyValue("environment", environment);
	}
}
