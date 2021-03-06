package org.naw.tasks;

import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.TaskContext;

public class Empty extends AbstractTask {

	public void run(TaskContext context, MessageExchange exchange) throws Exception {
		context.send(exchange);
	}

	public void recover(TaskContext context, MessageExchange exchange) throws Exception {
		run(context, exchange);
	}
}
