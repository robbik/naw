package org.naw.links.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.naw.core.task.support.Timer;
import org.naw.links.Link;
import org.naw.links.factory.LinkFactory;

public class DirectLinkFactory implements LinkFactory {
	
	private final Map<String, Link> links;
	
	private Timer timer;
	
	private long sendTimeout;
	
	public DirectLinkFactory() {
		links = Collections.synchronizedMap(new HashMap<String, Link>());
		
		sendTimeout = 10000;
	}
	
	public void setTimer(Timer timer) {
		this.timer = timer;
	}
	
	public void setSendTimeout(long sendTimeout) {
		this.sendTimeout = sendTimeout;
	}
	
	public Link createLink(String key) throws Exception {
		Link link;
		
		synchronized (links) {
			if (links.containsKey(key)) {
				link = links.get(key);
			} else {
				link = new DirectLink(timer, sendTimeout);
				links.put(key, link);
			}
		}
		
		return link;
	}
}
