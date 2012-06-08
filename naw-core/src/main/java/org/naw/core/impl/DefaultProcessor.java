package org.naw.core.impl;

import java.util.concurrent.TimeUnit;

import org.naw.core.Engine;
import org.naw.core.Processor;

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
		if (log.isTraceEnabled()) {
			log.trace("before processor running");
		}

		while (engine.getTaskQueue().next()) {
			Thread.yield();
		}
		
		if (log.isTraceEnabled()) {
			log.trace("after processor running");
		}
	}

	public boolean next() throws InterruptedException {
		return engine.getTaskQueue().next();
	}

	public boolean next(long timeout, TimeUnit unit) throws InterruptedException {
		return engine.getTaskQueue().next(timeout, unit);
	}
}
