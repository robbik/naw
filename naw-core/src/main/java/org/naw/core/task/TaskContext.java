package org.naw.core.task;

import org.naw.core.Engine;
import org.naw.core.exchange.MessageExchange;
import org.naw.executables.Executable;

public interface TaskContext {
	
	Engine getEngine();
	
	Executable getExecutable();

	TaskPipeline getPipeline();
	
	Task getTask();
	
	TaskContext getNext();

	void send(MessageExchange exchange);

	void run(MessageExchange exchange);
}
