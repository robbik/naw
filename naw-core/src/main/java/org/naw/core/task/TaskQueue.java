package org.naw.core.task;

import java.util.concurrent.TimeUnit;

public interface TaskQueue {

	void add(TaskContext context, DataExchange exchange);

	Entry remove() throws InterruptedException;

	Entry remove(long timeout, TimeUnit unit) throws InterruptedException;

	public static interface Entry {
		TaskContext getTaskContext();

		DataExchange getExchange();
	}
}
