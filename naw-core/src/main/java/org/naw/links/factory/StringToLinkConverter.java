package org.naw.links.factory;

import java.net.URI;

import org.naw.links.Links;

import rk.commons.inject.factory.ObjectFactory;
import rk.commons.inject.factory.type.converter.TypeConverter;
import rk.commons.util.UriHelper;

public class StringToLinkConverter implements TypeConverter {

	private final ObjectFactory factory;

	public StringToLinkConverter(ObjectFactory factory) {
		this.factory = factory;
	}

	public Object convert(Object source) {
		URI uri = UriHelper.tryNewURI((String) source);
		if (uri == null) {
			throw new IllegalArgumentException("invalid URI '" + source + "'");
		}
		
		return Links.lookup(factory, uri);
	}
}
