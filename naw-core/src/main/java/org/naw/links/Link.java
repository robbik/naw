package org.naw.links;

/**
 * Link with external entity
 */
public interface Link {

	/**
	 * send data to external entity
	 * 
	 * @param data
	 *            data to be sent
	 * @throws Exception
	 *             if an error occurred while sending
	 */
	void send(Object data) throws Exception;

	/**
	 * send data to external entity and return reference for receiving the
	 * response
	 * 
	 * @param data
	 *            data to be sent
	 * @return reference number to be looked up later using {@link
	 *         PartnerLink.receive(String)} method
	 * @throws Exception
	 *             if an error occurred while sending
	 */
	String sendAndReceive(Object data) throws Exception;

	/**
	 * receive response (and return immediately if the response has not ready
	 * yet)
	 * 
	 * @param ref
	 *            reference number returned from {@link
	 *            PartnerLink.sendAndReceive(Object)} method
	 * @return received response or <code>null</code> if the response has not
	 *         ready yet
	 * @throws Exception
	 *             if an error occurred while receiving
	 */
	Object receive(String ref) throws Exception;

	/**
	 * receive request from external entity (and return immediately if no
	 * request available)
	 * 
	 * @return received request or <code>null</code> if no request available
	 * @throws Exception
	 *             if an error occurred while receiving
	 */
	Object receive() throws Exception;
}
