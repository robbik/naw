package org.naw.core.impl;

import java.util.concurrent.TimeUnit;

import org.naw.core.Engine;
import org.naw.core.Processor;
import org.naw.core.task.TaskContext;
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

	public void run() throws InterruptedException {
		Entry entry;
		
		if (log.isTraceEnabled()) {
			log.trace("before processor running");
		}

		while ((entry = engine.getTaskQueue().remove()) != null) {
			TaskContext ctx = entry.getTaskContext();
			
			if (log.isTraceEnabled()) {
				log.trace("before executing task " + ctx.getTask());
			}
			
			try {
				ctx.getTask().run(ctx, entry.getExchange());
			} catch (Throwable t) {
				log.error("unable to execute task " + entry.getTaskContext(), t);
			}
			
			if (log.isTraceEnabled()) {
				log.trace("after executing task " + ctx.getTask());
			}
		}
		
		if (log.isTraceEnabled()) {
			log.trace("after processor running");
		}
	}

	public void runOnce() throws InterruptedException {
		Entry entry = engine.getTaskQueue().remove();

		if (entry != null) {
			TaskContext ctx = entry.getTaskContext();
			
			try {
				ctx.getTask().run(ctx, entry.getExchange());
			} catch (Throwable t) {
				log.error("unable to execute task " + entry.getTaskContext(), t);
			}
		}
	}

	public void runOnce(long timeout, TimeUnit unit) throws InterruptedException {
		Entry entry = engine.getTaskQueue().remove(timeout, unit);

		if (entry != null) {
			TaskContext ctx = entry.getTaskContext();
			
			try {
				ctx.getTask().run(ctx, entry.getExchange());
			} catch (Throwable t) {
				log.error("unable to execute task " + entry.getTaskContext(), t);
			}
		}
	}
}
