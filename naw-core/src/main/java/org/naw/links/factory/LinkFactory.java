package org.naw.links.factory;

import org.naw.links.Link;

public interface LinkFactory {

	Link createLink(String schemeSpecificPart) throws Exception;
}
