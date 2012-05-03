package org.naw.expression;

import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;

public abstract class Expression implements Task {
	
	public void run(TaskContext context, DataExchange exchange) throws Exception {
		execute(exchange);
		
		context.next(exchange);
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		run(context, exchange);
	}

	public abstract boolean getBoolean(DataExchange exchange) throws Exception;
	
	public abstract void execute(DataExchange exchange) throws Exception;
}
