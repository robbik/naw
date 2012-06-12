package org.naw.jndi.factory.xml;

import org.naw.jndi.factory.JndiRefObjectFactory;
import org.w3c.dom.Element;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;
import rk.commons.util.StringHelper;

public class JndiRefDefinitionParser extends SingleObjectDefinitionParser {
	
	public static final String ELEMENT_LOCAL_NAME = "jndi-ref";
	
	@Override
	protected Class<?> getObjectClass(Element element) {
		return JndiRefObjectFactory.class;
	}

	protected void doParse(Element element,
			ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		
		builder.setObjectName(element.getAttribute("name"));
		
		String jndiContextRef = element.getAttribute("jndiContext-ref");
		if (StringHelper.hasText(jndiContextRef)) {
			builder.addPropertyReference("jndiContext", jndiContextRef);
		}
		
		builder.addPropertyValue("jndiName", element.getAttribute("jndiName"));
	}
}
