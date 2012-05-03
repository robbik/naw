package org.naw.tasks;

import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;

public class Empty implements Task {

	public void run(TaskContext context, DataExchange exchange)
			throws Exception {
		context.next(exchange);
	}

	public void recover(TaskContext context, DataExchange exchange)
			throws Exception {
		run(context, exchange);
	}
}
