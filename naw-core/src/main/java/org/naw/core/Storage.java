package org.naw.core;

import java.util.Collection;

import org.naw.core.exchange.MessageExchange;
import org.naw.executables.Executable;

public interface Storage {
	
	MessageExchange createMessageExchange(Engine engine, Executable executable);
	
	void addTask(String mexId, String taskId);
	
	void removeTask(String mexId, String taskId);
	
	void addOnGoingTask(String mexId, String taskId);
	
	Collection<MessageExchange> getMessageExchanges();
	
	Collection<String> getTasks(String mexId);
	
	Collection<String> getOnGoingTasks(String mexId);
}
