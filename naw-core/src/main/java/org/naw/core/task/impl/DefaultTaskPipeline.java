package org.naw.core.task.impl;

import org.naw.core.Engine;
import org.naw.core.task.DataExchange;
import org.naw.core.task.LifeCycleAware;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskPipeline;
import org.naw.executables.Executable;

public class DefaultTaskPipeline implements TaskPipeline {
	
	private final Engine engine;
	
	private final Executable executable;
	
	private DefaultTaskContext head;
	
	private DefaultTaskContext tail;
	
	public DefaultTaskPipeline(Engine engine, Executable executable) {
		this.engine = engine;
		this.executable = executable;
		
		head = null;
		tail = null;
	}

	public Engine getEngine() {
		return engine;
	}

	public Executable getExecutable() {
		return executable;
	}
	
	public void fireBeforeAddEvent() {
		TaskContext current = head;
		
		while (current != null) {
			Task task = current.getTask();
			
			if (task instanceof LifeCycleAware) {
				((LifeCycleAware) task).beforeAdd(current);
			}
			
			current = current.getNext();
		}
	}

	public TaskPipeline addLast(Task task, boolean fireEvent) {
		if (task == null) {
			return this;
		}

		DefaultTaskContext ctx = new DefaultTaskContext(this, task);
		
		if (fireEvent && (task instanceof LifeCycleAware)) {
			((LifeCycleAware) task).beforeAdd(ctx);
		}
		
		if (head == null) {
			head = ctx;
		} else {
			tail.setNext(ctx);
		}
		
		tail = ctx;
		
		return this;
	}

	public TaskPipeline addLast(Task task) {
		return addLast(task, true);
	}

	public void start(DataExchange exchange) {
		if (head != null) {
			engine.getTaskQueue().add(head, exchange);
		}
	}

	public TaskPipeline addLast(TaskContext ctx) {
		if (ctx == null) {
			return this;
		}
		
		if (!(ctx instanceof DefaultTaskContext)) {
			TaskContext next = ctx.getNext(); 
			Task task = ctx.getTask();
			
			ctx = new DefaultTaskContext(this, task);
			((DefaultTaskContext) ctx).setNext(next);
		}
		
		if (tail == null) {
			head = (DefaultTaskContext) ctx;
		} else {
			tail.setNext(ctx);
		}
		
		tail = (DefaultTaskContext) ctx;

		return this;
	}
}
