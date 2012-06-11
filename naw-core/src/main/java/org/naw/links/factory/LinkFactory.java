package org.naw.links.factory;

import java.net.URI;

import org.naw.links.Link;

public interface LinkFactory {

	Link createLink(URI uri) throws Exception;
}
