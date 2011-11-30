package org.naw.core.partnerLink;

import java.io.Serializable;
import java.util.Map;

public interface MessageEvent extends Serializable {

	/**
	 * Get partner link that received this message
	 * 
	 * @return
	 */
	PartnerLink getPartnerLink();

	/**
	 * Get process id which sent this message
	 * 
	 * @return
	 */
	String getSource();

	/**
	 * Get process id where this message was sent to
	 * 
	 * @return
	 */
	String getDestination();

	/**
	 * get partner link operation
	 * 
	 * @return
	 */
	String getOperation();

	/**
	 * Get the sent message
	 * 
	 * @return
	 */
	Map<String, Object> getMessage();

}