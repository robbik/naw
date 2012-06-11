package org.naw.links;

import java.net.URI;

import org.naw.links.factory.LinkFactory;

import rk.commons.inject.factory.ObjectFactory;
import rk.commons.inject.factory.ObjectInstantiationException;

public abstract class Links {
	
	public static Link lookup(ObjectFactory factory, URI uri) {
		String scheme = uri.getScheme();
		
		Object linkFactory = factory.getObject(scheme);

		try {
			return ((LinkFactory) linkFactory).createLink(uri);
		} catch (Exception e) {
			throw new ObjectInstantiationException(scheme, e);
		}
	}
}
