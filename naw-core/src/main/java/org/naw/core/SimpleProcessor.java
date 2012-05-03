package org.naw.core;

import java.util.concurrent.TimeUnit;

import org.naw.core.task.TaskQueue.Entry;

import rk.commons.logging.Logger;
import rk.commons.logging.LoggerFactory;

public class SimpleProcessor implements Processor {
	
	private static final Logger log = LoggerFactory.getLogger(Processor.class);

	private final Engine engine;

	public SimpleProcessor(Engine engine) {
		this.engine = engine;
	}

	public Engine getEngine() {
		return engine;
	}

	public void run() throws InterruptedException {
		Entry entry;

		while ((entry = engine.getTaskQueue().remove()) != null) {
			try {
				entry.getTaskContext().run(entry.getExchange());
			} catch (Throwable t) {
				log.error("unable to execute task " + entry.getTaskContext(), t);
			}
		}
	}

	public void runOnce() throws InterruptedException {
		Entry entry = engine.getTaskQueue().remove();

		if (entry != null) {
			try {
				entry.getTaskContext().run(entry.getExchange());
			} catch (Throwable t) {
				log.error("unable to execute task " + entry.getTaskContext(), t);
			}
		}
	}

	public void runOnce(long timeout, TimeUnit unit) throws InterruptedException {
		Entry entry = engine.getTaskQueue().remove(timeout, unit);

		if (entry != null) {
			try {
				entry.getTaskContext().run(entry.getExchange());
			} catch (Throwable t) {
				log.error("unable to execute task " + entry.getTaskContext(), t);
			}
		}
	}
}
