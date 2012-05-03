package org.naw.core.task;

import java.util.concurrent.TimeUnit;

public interface TaskQueue {

	void add(TaskContext context, DataExchange exchange);

	TaskContextFuture addLater(TaskContext context, DataExchange exchange, long delay, TimeUnit unit);

	TaskContextFuture addLater(TaskContext context, DataExchange exchange, long deadline);

	Entry remove() throws InterruptedException;

	Entry remove(long timeout, TimeUnit unit) throws InterruptedException;

	public static interface Entry {
		TaskContext getTaskContext();

		DataExchange getExchange();
	}
}
