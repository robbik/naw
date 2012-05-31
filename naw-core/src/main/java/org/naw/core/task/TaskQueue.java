package org.naw.core.task;

import java.util.concurrent.TimeUnit;

import org.naw.core.exchange.MessageExchange;

public interface TaskQueue {

	void add(TaskContext context, MessageExchange exchange, boolean recoveryMode);

	Entry remove() throws InterruptedException;

	Entry remove(long timeout, TimeUnit unit) throws InterruptedException;

	public static interface Entry {
		TaskContext getTaskContext();

		MessageExchange getMessageExchange();
		
		TaskFuture getFuture();
		
		boolean isRecoveryMode();
	}
}
