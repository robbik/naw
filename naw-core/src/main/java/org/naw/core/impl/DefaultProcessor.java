package org.naw.core.impl;

import java.util.concurrent.TimeUnit;

import org.naw.core.Engine;
import org.naw.core.Processor;
import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskFuture;
import org.naw.core.task.TaskQueue.Entry;

import rk.commons.logging.Logger;
import rk.commons.logging.LoggerFactory;

public class DefaultProcessor implements Processor {
	
	private static final Logger log = LoggerFactory.getLogger(Processor.class);

	private final Engine engine;

	public DefaultProcessor(Engine engine) {
		this.engine = engine;
	}

	public Engine getEngine() {
		return engine;
	}
	
	private void run(Entry entry) {
		TaskContext ctx = entry.getTaskContext();
		MessageExchange mex = entry.getMessageExchange();
		TaskFuture future = entry.getFuture();
		
		Task task = ctx.getTask();
		
		if ((mex != null) && mex.isDestroyed()) {
			future.cancel();
			
			log.warning("skipping pending task " + task + " due to process termination");
			
			return;
		}
		
		future.beforeRun();
		
		if (log.isTraceEnabled()) {
			log.trace("before executing task " + task);
		}
		
		try {
			task.run(ctx, mex);
			
			future.setSuccess();
		} catch (Throwable t) {
			future.setFailure(t);
			
			log.error("an error occured while executing task " + task + ", process terminated.", t);
		}
		
		if (log.isTraceEnabled()) {
			log.trace("after executing task " + task);
		}
	}

	public void run() throws InterruptedException {
		Entry entry;
		
		if (log.isTraceEnabled()) {
			log.trace("before processor running");
		}

		while ((entry = engine.getTaskQueue().remove()) != null) {
			run(entry);
		}
		
		if (log.isTraceEnabled()) {
			log.trace("after processor running");
		}
	}

	public void runOnce() throws InterruptedException {
		Entry entry = engine.getTaskQueue().remove();

		if (entry != null) {
			run(entry);
		}
	}

	public void runOnce(long timeout, TimeUnit unit) throws InterruptedException {
		Entry entry = engine.getTaskQueue().remove(timeout, unit);

		if (entry != null) {
			run(entry);
		}
	}
}
