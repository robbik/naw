package org.naw.core.task;

import org.naw.core.exchange.MessageExchange;

public interface Task {
	
	String getId();

	void run(TaskContext context, MessageExchange exchange) throws Exception;
	
	void recover(TaskContext context, MessageExchange exchange) throws Exception;
}
