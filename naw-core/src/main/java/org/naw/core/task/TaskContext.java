package org.naw.core.task;

import org.naw.core.Engine;
import org.naw.executables.Executable;

public interface TaskContext {
	
	Engine getEngine();
	
	Executable getExecutable();

	TaskPipeline getPipeline();
	
	Task getTask();
	
	TaskContext getNext();

	void forward(DataExchange exchange);

	void start(DataExchange exchange);
}
