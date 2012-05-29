package org.naw.links.factory;

import rk.commons.inject.factory.ObjectFactory;
import rk.commons.inject.factory.ObjectInstantiationException;
import rk.commons.inject.factory.type.converter.TypeConverter;

public class StringToLinkConverter implements TypeConverter {

	private final ObjectFactory factory;

	public StringToLinkConverter(ObjectFactory factory) {
		this.factory = factory;
	}

	public Object convert(Object source) {
		String scheme;
		String arg;
		
		String str = (String) source;
		
		int ddidx = str.indexOf(':');
		if (ddidx >= 0) {
			scheme = str.substring(0, ddidx).trim();
			arg = str.substring(ddidx + 1).trim();
		} else {
			scheme = str.trim();
			arg = "";
		}

		Object linkFactory = factory.getObject(scheme);

		try {
			return ((LinkFactory) linkFactory).createLink(arg);
		} catch (Exception e) {
			throw new ObjectInstantiationException(scheme, e);
		}
	}
}
