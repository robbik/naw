package org.naw.core.task;

import java.util.concurrent.TimeUnit;

import org.naw.core.Engine;
import org.naw.core.exchange.MessageExchange;

import rk.commons.inject.factory.ObjectFactory;

public interface TaskQueue {
	
	void attach(Engine engine, ObjectFactory factory) throws Exception;
	
	void detach();

	void add(TaskContext context, MessageExchange exchange);

	boolean next() throws InterruptedException;

	boolean next(long timeout, TimeUnit unit) throws InterruptedException;
}
