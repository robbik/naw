package org.naw.links;

public class LinkExchange {

	private Object correlation;

	private Link link;

	public LinkExchange() {
		// do nothing
	}

	public LinkExchange(Object correlation, Link link) {
		this.correlation = correlation;
		this.link = link;
	}

	public Object getCorrelation() {
		return correlation;
	}

	public void setCorrelation(Object correlation) {
		this.correlation = correlation;
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}
}
