package org.naw.links.factory;

import java.net.URI;

import rk.commons.inject.factory.IocObjectFactory;
import rk.commons.inject.factory.ObjectInstantiationException;
import rk.commons.inject.factory.ObjectNotFoundException;
import rk.commons.inject.factory.type.converter.TypeConverter;
import rk.commons.util.StringUtils;

public class StringToLinkConverter implements TypeConverter {

	private final IocObjectFactory factory;

	public StringToLinkConverter(IocObjectFactory factory) {
		this.factory = factory;
	}

	public Object convert(Object source) {
		URI uri = URI.create((String) source);

		String scheme = uri.getScheme();
		if (!StringUtils.hasText(scheme)) {
			throw new ObjectNotFoundException(scheme);
		}

		scheme = scheme.trim();

		Object linkFactory = factory.getObject(scheme);

		try {
			return ((LinkFactory) linkFactory).createLink(uri.getSchemeSpecificPart());
		} catch (Exception e) {
			throw new ObjectInstantiationException(scheme, e);
		}
	}
}
