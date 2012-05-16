package org.naw.links;

import org.naw.exceptions.LinkException;

/**
 * Link with external entity
 */
public interface Link {
	
	String getArgument();

	/**
	 * send data to external entity
	 * 
	 * @param message
	 *            data to be sent
	 * @throws Exception
	 *             if an error occurred while sending
	 */
	void send(Message message) throws LinkException, Exception;
	
	void sendReply(Message message) throws LinkException, Exception;

	/**
	 * receive data from external entity (and return immediately if the data is
	 * not available yet)
	 * 
	 * @param correlation
	 *            correlation returned by invoking {@link Link.send(Object,
	 *            boolean)} method with oneWay argument is <code>true</code>. or
	 *            <code>null</code> if this is not correlated with any send
	 *            operation.
	 * @return received data or <code>null</code> if the data is not available
	 *         yet
	 * @throws Exception
	 *             if an error occurred while receiving
	 */
	LinkAsyncResult asyncReceive(Object correlation, Object attachment, long deadline, AsyncCallback<Message> callback) throws Exception;
	
	LinkAsyncResult asyncReceiveReply(Object correlation, Object attachment, long deadline, AsyncCallback<Message> callback) throws Exception;
}
