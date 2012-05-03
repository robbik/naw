package org.naw.core.task;

import java.util.concurrent.TimeUnit;

import org.naw.core.Engine;
import org.naw.core.task.support.TaskContextUtils;
import org.naw.executables.Executable;

public class SimpleTaskContext implements TaskContext {

	private final Engine engine;

	private final Executable executable;

	private final Task task;

	private TaskContext next;

	public SimpleTaskContext(Engine engine, Executable executable, Task task) {
		this.engine = engine;
		this.executable = executable;

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

	public Engine getEngine() {
		return engine;
	}

	public Executable getExecutable() {
		return executable;
	}
	
	public TaskContext getNextTaskContext() {
		return next;
	}

	public void next(DataExchange exchange) {
		if (next != null) {
			engine.getTaskQueue().add(next, exchange);
		}
	}

	public TaskContextFuture nextLater(DataExchange exchange, long delay, TimeUnit unit) {
		if (next == null) {
			return TaskContextUtils.createFinishedFuture();
		} else {
			return engine.getTaskQueue().addLater(next, exchange, delay, unit);
		}
	}

	public TaskContextFuture nextLater(DataExchange exchange, long deadline) {
		if (next == null) {
			return TaskContextUtils.createFinishedFuture();
		} else {
			return engine.getTaskQueue().addLater(next, exchange, deadline);
		}
	}

	public void start(DataExchange exchange) {
		engine.getTaskQueue().add(this, exchange);
	}

	public TaskContextFuture startLater(DataExchange exchange, long delay, TimeUnit unit) {
		return engine.getTaskQueue().addLater(this, exchange, delay, unit);
	}
	
	public TaskContextFuture startLater(DataExchange exchange, long deadline) {
		return engine.getTaskQueue().addLater(this, exchange, deadline);
	}

	public void run(DataExchange exchange) throws Exception {
		task.run(this, exchange);
	}
	
	@Override
	public String toString() {
		return "{ Task = " + task + ", executable = " + executable + " }";
	}
}
