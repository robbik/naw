package org.naw.links.factory.xml;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import rk.commons.ioc.factory.support.ObjectDefinitionBuilder;
import rk.commons.ioc.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.ioc.factory.xml.SingleObjectDefinitionParser;

public class LinkFactoryDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "link";
	
	private Set<String> reserved;
	
	public LinkFactoryDefinitionParser() {
		reserved = new HashSet<String>();
		
		reserved.add("name");
		reserved.add("name-ref");
		reserved.add("class");
		reserved.add("class-ref");
	}
	
	@Override
	protected String getObjectClassName(Element element) {
		return element.getAttribute("class");
	}
	
	protected void doParse(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		builder.setObjectQName(element.getAttribute("name"));
		
		NamedNodeMap attrs = element.getAttributes();
		
		for (int i = 0, n = attrs.getLength(); i < n; ++i) {
			Attr attr = (Attr) attrs.item(i);
			
			String name = delegate.getLocalName(attr);
			
			if (!reserved.contains(name)) {
				if (name.endsWith("-ref")) {
					builder.addPropertyReference(name.substring(0, name.length() - 4), attr.getValue());
				} else {
					builder.addPropertyValue(name, attr.getValue());
				}
			}
		}
	}
}
