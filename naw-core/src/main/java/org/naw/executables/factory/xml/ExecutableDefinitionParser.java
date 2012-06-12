package org.naw.executables.factory.xml;

import java.util.ArrayList;
import java.util.List;

import org.naw.executables.Executable;
import org.osgi.framework.Version;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;
import rk.commons.util.StringHelper;

public class ExecutableDefinitionParser extends SingleObjectDefinitionParser {
	
	public static final String ELEMENT_LOCAL_NAME = "process";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Executable.class;
	}

	protected void doParse(Element element,
			ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		
		String name = element.getAttribute("name");
		
		Version version;
		
		if (StringHelper.hasText(element.getAttribute("version"))) {
			version = Version.parseVersion(element.getAttribute("version"));
		} else {
			version = Version.emptyVersion;
		}
		
		String oldPrefix = delegate.getObjectNamePrefix();
		String oldSuffix = delegate.getObjectNameSuffix();
		
		delegate.setObjectNamePrefix(name + ":");
		delegate.setObjectNameSuffix(":" + version.toString());
		
		List<Object> tasks = new ArrayList<Object>();
		
		NodeList childNodes = element.getChildNodes();
		
		for (int i = 0, n = childNodes.getLength(); i < n; ++i) {
			Node childNode = childNodes.item(i);
			
			if (childNode instanceof Element) {
				tasks.add(delegate.parse((Element) childNode));
			}
		}
		
		delegate.setObjectNamePrefix(oldPrefix);
		delegate.setObjectNameSuffix(oldSuffix);
		
		builder.setObjectName(name + ":" + version.toString());
		
		builder.addPropertyValue("name", name);
		builder.addPropertyValue("version", version);
		
		builder.addPropertyValue("tasks", tasks);
	}
}
