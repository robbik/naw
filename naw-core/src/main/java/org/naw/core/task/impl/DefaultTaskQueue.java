package org.naw.core.task.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.naw.core.Storage;
import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskFuture;
import org.naw.core.task.TaskQueue;

import rk.commons.logging.Logger;
import rk.commons.logging.LoggerFactory;

public class DefaultTaskQueue implements TaskQueue {

	private static final Logger log = LoggerFactory.getLogger(DefaultTaskQueue.class);

	private final BlockingQueue<Object> queue;

	public DefaultTaskQueue() {
		this(new ArrayBlockingQueue<Object>(5000, true));
	}

	public DefaultTaskQueue(BlockingQueue<Object> queue) {
		this.queue = queue;
	}

	public void add(TaskContext context, MessageExchange mex, boolean recoveryMode) {
		if (log.isTraceEnabled()) {
			log.trace("adding task " + context.getTask() + " to execution queue");
		}
		
		String mexId;
		String taskId;
		
		if (mex == null) {
			mexId = null;
			taskId = null;
		} else {
			mexId = mex.getId();
			taskId = context.getTask().getId();
		}
		
		Storage storage = context.getStorage();
		
		DefaultTaskFuture future = new DefaultTaskFuture(storage, mexId, taskId);
		
		future.beforeAdd();
		
		boolean enqueued = queue.add(new DefaultEntry(context, mex, future, recoveryMode));
		
		if (enqueued) {
			if (log.isTraceEnabled()) {
				log.trace("task " + context.getTask() + " added to execution queue");
			}
		} else {
			future.cancel();

			log.error("unable to enqueue task " + context.getTask());
		}
	}

	public Entry remove() throws InterruptedException {
		if (log.isTraceEnabled()) {
			log.trace("taking task from execution queue");
		}
		
		Entry e = (Entry) queue.take();
		
		if (log.isTraceEnabled()) {
			log.trace("task " + e.getTaskContext().getTask() + " taken from execution queue");
		}
		
		return e;
	}

	public Entry remove(long timeout, TimeUnit unit) throws InterruptedException {
		return (Entry) queue.poll(timeout, unit);
	}

	public Entry poll() {
		return(Entry) queue.poll();
	}
	
	public static class DefaultEntry implements Entry {
		final TaskContext taskContext;

		final MessageExchange exchange;
		
		final TaskFuture future;
		
		final boolean recoveryMode;

		public DefaultEntry(TaskContext taskContext, MessageExchange exchange,
				TaskFuture future, boolean recoveryMode) {
			
			this.taskContext = taskContext;
			this.exchange = exchange;
			
			this.future = future;
			this.recoveryMode = recoveryMode;
		}

		public TaskContext getTaskContext() {
			return taskContext;
		}

		public MessageExchange getMessageExchange() {
			return exchange;
		}
		
		public TaskFuture getFuture() {
			return future;
		}
		
		public boolean isRecoveryMode() {
			return recoveryMode;
		}
	}
}
