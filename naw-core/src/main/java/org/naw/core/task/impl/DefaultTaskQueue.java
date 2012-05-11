package org.naw.core.task.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.naw.core.task.DataExchange;
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

	public void add(TaskContext context, DataExchange exchange) {
		boolean enqueued = queue.add(new EntryImpl(context, exchange));

		if (!enqueued) {
			log.error("unable to enqueue task " + context.getTask());
		}
	}

	public Entry remove() throws InterruptedException {
		return (Entry) queue.take();
	}

	public Entry remove(long timeout, TimeUnit unit) throws InterruptedException {
		return (Entry) queue.poll(timeout, unit);
	}

	public Entry poll() {
		return(Entry) queue.poll();
	}

	private static class EntryImpl implements Entry {
		final TaskContext taskContext;

		final DataExchange exchange;

		EntryImpl(TaskContext taskContext, DataExchange exchange) {
			this.taskContext = taskContext;
			this.exchange = exchange;
		}

		public TaskContext getTaskContext() {
			return taskContext;
		}

		public DataExchange getExchange() {
			return exchange;
		}
	}
}
