package org.naw.core.task.impl;

import org.naw.core.Engine;
import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskPipeline;
import org.naw.executables.Executable;

public class DefaultTaskContext implements TaskContext {

	private final TaskPipeline pipeline;

	private Task task;

	private TaskContext next;

	public DefaultTaskContext(TaskPipeline pipeline, Task task) {
		this.pipeline = pipeline;
		this.task = task;
	}

	public Task getTask() {
		return task;
	}
	
	public TaskContext getNext() {
		return next;
	}

	public void setNext(TaskContext next) {
		this.next = next;
	}

	public TaskPipeline getPipeline() {
		return pipeline;
	}
	
	public Engine getEngine() {
		return pipeline.getEngine();
	}
	
	public Executable getExecutable() {
		return pipeline.getExecutable();
	}

	public void forward(DataExchange exchange) {
		if (next != null) {
			pipeline.getEngine().getTaskQueue().add(next, exchange);
		}
	}

	public void start(DataExchange exchange) {
		pipeline.getEngine().getTaskQueue().add(this, exchange);
	}
	
	@Override
	public String toString() {
		return "{ Task = " + task + ", pipeline = " + pipeline + " }";
	}
}
