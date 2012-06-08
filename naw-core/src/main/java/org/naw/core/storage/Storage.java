package org.naw.core.storage;

import java.util.Collection;

import org.naw.core.exchange.MessageExchange;

public interface Storage {
	
	Collection<StoredTask> getTasks();
	
	void persist(String taskId, MessageExchange mex, int status);
	
	void remove(String taskId, String mexId);
	
	void update(String taskId, String mexId, int newStatus);
}
