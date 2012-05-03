package org.naw.links.spi;

import org.naw.links.Link;
import org.naw.links.factory.LinkFactory;

public class FileLinkFactory implements LinkFactory {

	public Link createLink(String schemeSpecificPart) throws Exception {
		return new FileLink(schemeSpecificPart);
	}
}
