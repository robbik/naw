package org.naw.core.storage;

import org.naw.core.exchange.MessageExchange;

public interface StoredTask {
	
	public static final int STATUS_PENDING = 0;

	public static final int STATUS_RUNNING = 1;

	String getTaskId();
	
	MessageExchange getMessageExchange();
	
	int getStatus();
	
	void setStatus(int status);
}
