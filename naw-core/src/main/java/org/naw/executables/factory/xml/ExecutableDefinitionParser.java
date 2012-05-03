package org.naw.executables.factory.xml;

import java.util.ArrayList;
import java.util.List;

import org.naw.executables.Executable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rk.commons.ioc.factory.support.ObjectDefinitionBuilder;
import rk.commons.ioc.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.ioc.factory.xml.SingleObjectDefinitionParser;

public class ExecutableDefinitionParser extends SingleObjectDefinitionParser {
	
	public static final String ELEMENT_LOCAL_NAME = "process";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Executable.class;
	}

	protected void doParse(Element element,
			ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		
		String name = element.getAttribute("name");
		String oldPackageName = delegate.getPackageName();
		
		delegate.setPackageName(name);
		
		List<Object> tasks = new ArrayList<Object>();
		
		NodeList childNodes = element.getChildNodes();
		
		for (int i = 0, n = childNodes.getLength(); i < n; ++i) {
			Node childNode = childNodes.item(i);
			
			if (childNode instanceof Element) {
				tasks.add(delegate.parse((Element) childNode));
			}
		}
		
		delegate.setPackageName(oldPackageName);
		
		builder.setObjectQName(name);
		
		builder.addPropertyValue("executableName", name);
		builder.addPropertyValue("tasks", tasks);
	}
}
