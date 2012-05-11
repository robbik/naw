package org.naw.core.task;

import org.naw.core.Engine;
import org.naw.executables.Executable;

public interface TaskPipeline {

	Engine getEngine();

	Executable getExecutable();

	void start(DataExchange exchange);
	
	TaskPipeline addLast(Task task);
	
	TaskPipeline addLast(TaskContext ctx);
}
