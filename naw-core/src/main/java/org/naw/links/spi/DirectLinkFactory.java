package org.naw.links.spi;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.naw.core.task.support.Timer;
import org.naw.links.Link;
import org.naw.links.factory.LinkFactory;

public class DirectLinkFactory implements LinkFactory {
	
	protected final Map<URI, Link> links;
	
	protected Timer timer;
	
	protected long sendTimeout;
	
	public DirectLinkFactory() {
		links = Collections.synchronizedMap(new HashMap<URI, Link>());
		
		sendTimeout = 10000;
	}
	
	public void setTimer(Timer timer) {
		this.timer = timer;
	}
	
	public void setSendTimeout(long sendTimeout) {
		this.sendTimeout = sendTimeout;
	}
	
	public Link createLink(URI uri) throws Exception {
		Link link;
		
		synchronized (links) {
			if (links.containsKey(uri)) {
				link = links.get(uri);
			} else {
				link = new DirectLink(timer, sendTimeout, uri);
				links.put(uri, link);
			}
		}
		
		return link;
	}
}
