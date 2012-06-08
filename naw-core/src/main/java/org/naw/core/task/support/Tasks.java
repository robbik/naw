package org.naw.core.task.support;

import java.util.List;

import org.naw.core.Engine;
import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskPipeline;
import org.naw.core.task.impl.DefaultTaskPipeline;
import org.naw.executables.Executable;

public abstract class Tasks {

	public static TaskPipeline pipeline(Engine engine, Executable executable, Task... tasks) {
		DefaultTaskPipeline pipeline = new DefaultTaskPipeline(engine, executable);
		
		for (int i = 0; i < tasks.length; ++i) {
			pipeline.addLast(tasks[i]);
		}
		
		return pipeline;
	}
	
	public static TaskPipeline pipeline(Engine engine, Executable executable, List<Task> tasks) {
		DefaultTaskPipeline pipeline = new DefaultTaskPipeline(engine, executable);
		
		int n = tasks.size();
		
		for (int i = 0; i < n; ++i) {
			pipeline.addLast(tasks.get(i), false);
		}
		
		pipeline.fireBeforeAddEvent();

		return pipeline;
	}
	
	public static void send(TaskPipeline pipeline, MessageExchange mex) {
		if (pipeline == null) {
			return;
		}
		
		TaskContext first = pipeline.getFirst();
		if (first == null) {
			return;
		}
		
		pipeline.getEngine().getTaskQueue().add(first, mex);
	}
}
