package org.naw.tasks.factory.xml;

import org.naw.tasks.Wait;
import org.w3c.dom.Element;

import rk.commons.inject.factory.support.ObjectDefinitionBuilder;
import rk.commons.inject.factory.xml.ObjectDefinitionParserDelegate;
import rk.commons.inject.factory.xml.SingleObjectDefinitionParser;
import rk.commons.util.StringUtils;

public class WaitDefinitionParser extends SingleObjectDefinitionParser {

	public static final String ELEMENT_LOCAL_NAME = "wait";

	@Override
	protected Class<?> getObjectClass(Element element) {
		return Wait.class;
	}

	protected void doParse(Element element, ObjectDefinitionParserDelegate delegate, ObjectDefinitionBuilder builder) {
		int forUntil = 0;
		
		builder.setObjectQName(element.getAttribute("name"));

		String tstr = element.getAttribute("until");
		if (StringUtils.hasText(tstr)) {
			builder.addPropertyValue("deadline", tstr.trim());
			
			++forUntil;
		}

		tstr = element.getAttribute("for");
		if (StringUtils.hasText(tstr)) {
			builder.addPropertyValue("duration", tstr.trim());
			
			++forUntil;
		}
		
		if (forUntil == 0) {
			throw new IllegalArgumentException("either for or until must be specified");
		} else if (forUntil > 1) {
			throw new IllegalArgumentException("until cannot be appeared if for is specified, and vice versa");
		}
	}
}
