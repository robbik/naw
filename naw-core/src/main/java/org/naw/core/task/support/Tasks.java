package org.naw.core.task.support;

import java.util.List;

import org.naw.core.Engine;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContextFuture;
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
	
	public static TaskContextFuture createFuture(final Timeout timeout) {
		return new TaskContextFuture() {
			
			public void cancel() {
				timeout.cancel();
			}
		};
	}
	
	public static TaskContextFuture createFinishedFuture() {
		return new TaskContextFuture() {
			
			public void cancel() {
				// do nothing
			}
		};
	}
}
