package org.naw.core.task;

import java.util.concurrent.TimeUnit;

import org.naw.core.Engine;
import org.naw.core.exchange.MessageExchange;

public interface TaskQueue {
	
	void attach(Engine engine) throws Exception;
	
	void detach(Engine engine);

	void add(TaskContext context, MessageExchange exchange);

	boolean next() throws InterruptedException;

	boolean next(long timeout, TimeUnit unit) throws InterruptedException;
}
