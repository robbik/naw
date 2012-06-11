package org.naw.links;

import java.io.Serializable;
import java.net.URI;

import rk.commons.inject.factory.ObjectFactory;

public class LinkExchange implements Serializable {

	private static final long serialVersionUID = 1L;

	private Object correlation;
	
	private URI linkURI;

	private transient Link link;

	public LinkExchange() {
		// do nothing
	}

	public LinkExchange(Object correlation, Link link) {
		this.correlation = correlation;
		
		this.link = link;
		
		if (link == null) {
			this.linkURI = null;
		} else {
			this.linkURI = link.getURI();
		}
	}

	public Object getCorrelation() {
		return correlation;
	}

	public void setCorrelation(Object correlation) {
		this.correlation = correlation;
	}

	public Link getLink(ObjectFactory factory) {
		if (linkURI == null) {
			return null;
		} else {
			if (link == null) {
				link = Links.lookup(factory, linkURI);
			}
		}
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
		
		if (link == null) {
			this.linkURI = null;
		} else {
			this.linkURI = link.getURI();
		}
	}
}
