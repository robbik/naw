package org.naw.links;

public class LinkAsyncResult extends DefaultAsyncResult<Message> {

	protected final Link link;

	public LinkAsyncResult(Link link, Object attachment) {
		super(attachment);

		this.link = link;
	}

	public Link getLink() {
		return link;
	}
}
