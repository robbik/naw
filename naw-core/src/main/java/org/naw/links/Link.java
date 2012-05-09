package org.naw.links;

import org.naw.exceptions.LinkException;

/**
 * Link with external entity
 */
public interface Link {

	/**
	 * send data to external entity
	 * 
	 * @param data
	 *            data to be sent
	 * @param oneWay
	 *            <code>true</code> if this operation is one way,
	 *            <code>false</code> otherwise.
	 * @return <code>null</code> if this operation is one way, correlation id
	 *         otherwise.
	 * @throws Exception
	 *             if an error occurred while sending
	 */
	Object send(Object data, boolean oneWay) throws LinkException, Exception;

	/**
	 * receive data from external entity (and return immediately if the data is
	 * not available yet)
	 * 
	 * @param correlationId
	 *            correlation id returned by invoking {@link Link.send(Object,
	 *            boolean)} method with oneWay argument is <code>true</code>. or
	 *            <code>null</code> if this is not correlated with any send
	 *            operation.
	 * @return received data or <code>null</code> if the data is not available
	 *         yet
	 * @throws Exception
	 *             if an error occurred while receiving
	 */
	Object receive(Object correlationId) throws LinkException, Exception;
}
