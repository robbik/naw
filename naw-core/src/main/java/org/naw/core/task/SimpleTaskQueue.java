package org.naw.core.task;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.naw.core.task.support.HashedWheelTimer;
import org.naw.core.task.support.TaskContextUtils;
import org.naw.core.task.support.Timeout;
import org.naw.core.task.support.Timer;
import org.naw.core.task.support.TimerTask;

import rk.commons.logging.Logger;
import rk.commons.logging.LoggerFactory;

public class SimpleTaskQueue implements TaskQueue {

	private static final Logger log = LoggerFactory.getLogger(SimpleTaskQueue.class);

	private final Timer timer;

	private final BlockingQueue<Object> queue;

	public SimpleTaskQueue() {
		this(new HashedWheelTimer(), new ArrayBlockingQueue<Object>(5000, true));
	}

	public SimpleTaskQueue(Timer timer) {
		this(timer, new ArrayBlockingQueue<Object>(5000, true));
	}

	public SimpleTaskQueue(BlockingQueue<Object> queue) {
		this(new HashedWheelTimer(), queue);
	}

	public SimpleTaskQueue(Timer timer, BlockingQueue<Object> queue) {
		this.timer = timer;
		this.queue = queue;
	}

	public Timer getTimer() {
		return timer;
	}

	public void add(TaskContext context, DataExchange exchange) {
		boolean enqueued = queue.add(new EntryImpl(context, exchange));

		if (!enqueued) {
			log.error("unable to enqueue task " + context.getTask());
		}
	}

	public TaskContextFuture addLater(final TaskContext context,
			final DataExchange exchange, long delay, TimeUnit unit) {

		if (delay == 0) {
			add(context, exchange);

			return TaskContextUtils.createFinishedFuture();
		} else {
			return TaskContextUtils.createFuture(timer.newTimeout(
					new TimerTask() {

						public void run(Timeout timeout) throws Exception {
							add(context, exchange);
						}
					}, delay, unit));
		}
	}

	public TaskContextFuture addLater(final TaskContext context,
			final DataExchange exchange, long deadline) {

		long now = System.currentTimeMillis();

		if (deadline <= now) {
			add(context, exchange);

			return TaskContextUtils.createFinishedFuture();
		} else {
			return TaskContextUtils.createFuture(timer.newTimeout(
					new TimerTask() {

						public void run(Timeout timeout) throws Exception {
							add(context, exchange);
						}
					}, deadline));
		}
	}

	public Entry remove() throws InterruptedException {
		return (Entry) queue.take();
	}

	public Entry remove(long timeout, TimeUnit unit)
			throws InterruptedException {
		return (Entry) queue.poll(timeout, unit);
	}

	public Entry poll() {
		return (Entry) queue.poll();
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
