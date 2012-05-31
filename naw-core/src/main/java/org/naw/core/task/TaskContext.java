package org.naw.core.task;

import org.naw.core.Engine;
import org.naw.core.Storage;
import org.naw.core.exchange.MessageExchange;
import org.naw.executables.Executable;

public interface TaskContext {
	
	Engine getEngine();
	
	Storage getStorage();
	
	Executable getExecutable();

	TaskPipeline getPipeline();
	
	Task getTask();
	
	TaskContext getNext();

	void send(MessageExchange exchange);

	void run(MessageExchange exchange);
	
	void recover(MessageExchange exchange);
}
