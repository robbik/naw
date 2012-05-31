package org.naw.core.task;

import org.naw.core.Engine;
import org.naw.executables.Executable;

public interface TaskPipeline {

	Engine getEngine();

	Executable getExecutable();

	TaskContext getFirst();
	
	TaskPipeline addLast(Task task);
	
	TaskPipeline addLast(TaskContext ctx);
	
	TaskContext getTaskContext(Task task);
	
	TaskContext getTaskContext(String taskId);
}
