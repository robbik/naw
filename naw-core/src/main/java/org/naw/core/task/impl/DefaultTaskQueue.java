package org.naw.core.task.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.naw.core.Engine;
import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
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

	public void attach(Engine engine) {
		// do nothing
	}
	
	public void detach(Engine engine) {
		// do nothing
	}
	
	public void add(TaskContext context, MessageExchange mex) {
		if (log.isTraceEnabled()) {
			log.trace("adding task " + context.getTask() + " to execution queue");
		}
		
		boolean enqueued = queue.add(new Entry(context, mex));
		
		if (enqueued) {
			if (log.isTraceEnabled()) {
				log.trace("task " + context.getTask() + " added to execution queue");
			}
		} else {
			log.error("unable to enqueue task " + context.getTask());
		}
	}

	public boolean next() throws InterruptedException {
		Entry e = (Entry) queue.take();
		run(e);
		
		return true;
	}

	public boolean next(long timeout, TimeUnit unit) throws InterruptedException {
		Entry e = (Entry) queue.poll(timeout, unit);
		if (e == null) {
			return false;
		}
		
		run(e);
		
		return true;
	}
	
	private void run(Entry entry) {
		TaskContext ctx = entry.taskContext;
		MessageExchange mex = entry.messageExchange;
		
		Task task = ctx.getTask();
		
		if (log.isTraceEnabled()) {
			log.trace("before executing task " + task);
		}
		
		try {
			task.run(ctx, mex);
		} catch (Throwable t) {
			log.error("an error occured while executing task " + task + ", process terminated.", t);
		}
		
		if (log.isTraceEnabled()) {
			log.trace("after executing task " + task);
		}
	}
	
	private static class Entry {
		final TaskContext taskContext;

		final MessageExchange messageExchange;

		Entry(TaskContext taskContext, MessageExchange exchange) {
			this.taskContext = taskContext;
			this.messageExchange = exchange;
		}
	}
}
