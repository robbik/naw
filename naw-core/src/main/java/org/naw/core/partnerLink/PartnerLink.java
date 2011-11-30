package org.naw.core.partnerLink;

import java.util.Map;

/**
 * Workflow partner link
 */
public interface PartnerLink {

	/**
	 * subscribe listener for specific partner link operation
	 * 
	 * @param operation
	 *            partner link operation
	 * @param listener
	 *            the listener
	 */
	void subscribe(String operation, PartnerLinkListener listener);

	/**
	 * Publish (broadcast) message
	 * 
	 * @param source
	 *            source of the message (who publish the message, the workflow
	 *            instance id)
	 * @param operation
	 *            partner link related operation
	 * @param message
	 *            message published
	 */
	void publish(String source, String operation, Map<String, Object> message);

	/**
	 * Send message to specific destination (workflow instance id)
	 * 
	 * @param source
	 *            source of the message (who publish the message, the workflow
	 *            instance id)
	 * @param destination
	 *            destination of the message (the workflow instance id)
	 * @param operation
	 *            partner link related operation
	 * @param message
	 *            message published
	 */
	void send(String source, String destination, String operation,
			Map<String, Object> message);

	/**
	 * un-subscribe listener from specific partner link operation
	 * 
	 * @param operation
	 *            partner link operation
	 * @param listener
	 *            the listener
	 */
	void unsubscribe(String operation, PartnerLinkListener listener);
}
