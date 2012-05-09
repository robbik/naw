package org.naw.exceptions;

import org.naw.links.Link;

public class LinkException extends RuntimeException {

	private static final long serialVersionUID = 134256551020662918L;
	
	private final Link link;
	
	private final int errorCode;

	public LinkException(Link link, int errorCode) {
		this.link = link;
		this.errorCode = errorCode;
	}

	public LinkException(Link link, int errorCode, String message) {
		super(message);
		
		this.link = link;
		this.errorCode = errorCode;
	}

	public LinkException(Link link, int errorCode, Throwable cause) {
		super(cause);
		
		this.link = link;
		this.errorCode = errorCode;
	}

	public LinkException(Link link, int errorCode, String message, Throwable cause) {
		super(message, cause);
		
		this.link = link;
		this.errorCode = errorCode;
	}

	public Link getLink() {
		return link;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}
