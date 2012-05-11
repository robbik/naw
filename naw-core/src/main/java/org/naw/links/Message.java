package org.naw.links;

public class Message {

	private Object correlation;
	
	private Object body;
	
	public Message() {
		this(null, null);
	}
	
	public Message(Object body) {
		this(null, body);
	}
	
	public Message(Object correlation, Object body) {
		this.correlation = correlation;
		this.body = body;
	}

	public Object getCorrelation() {
		return correlation;
	}

	public Object getBody() {
		return body;
	}
	
	@Override
	public String toString() {
		return super.toString() + " [ correlation: " + correlation + ", body: " + body + " ]";
	}
}
