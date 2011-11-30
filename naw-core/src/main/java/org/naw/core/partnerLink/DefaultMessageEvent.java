package org.naw.core.partnerLink;

import java.util.Map;

public class DefaultMessageEvent implements MessageEvent {

	private static final long serialVersionUID = -4628669083071464389L;

	private PartnerLink partnerLink;

	private String operation;

	private String source;

	private String destination;

	private Map<String, Object> message;

	public DefaultMessageEvent() {
		// do nothing
	}

	public DefaultMessageEvent(PartnerLink partnerLink, String source,
			String destination, String operation, Map<String, Object> message) {
		this.partnerLink = partnerLink;

		this.operation = operation;
		this.source = source;
		this.destination = destination;

		this.message = message;
	}

	public PartnerLink getPartnerLink() {
		return partnerLink;
	}

	public void setPartnerLink(PartnerLink partnerLink) {
		this.partnerLink = partnerLink;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Map<String, Object> getMessage() {
		if (message == null) {
			return null;
		}

		return message;
	}

	public void setMessage(Map<String, Object> message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return super.toString() + " [source: " + source + "\ndestination: "
				+ destination + "\noperation: " + operation + "\nmessage:\n"
				+ message + "]";
	}
}
