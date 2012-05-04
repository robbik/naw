package org.naw.tasks;

import java.util.List;

import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.support.TaskContextUtils;
import org.naw.expression.Expression;

public class While implements Task {

	private Expression predicate;

	private List<Task> bodyTasks;
	
	private volatile TaskContext bodyEntryPoint;

	public void setPredicate(Expression predicate) {
		this.predicate = predicate;
	}

	public void setBodyTasks(List<Task> bodyTasks) {
		this.bodyTasks = bodyTasks;
	}

	private synchronized void initialize(TaskContext context) {
		if (bodyEntryPoint == null) {
			bodyEntryPoint = TaskContextUtils.getFirstTaskContext(context, bodyTasks);
			bodyTasks = null;
	
			TaskContextUtils.addLast(bodyEntryPoint, context);
		}
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		initialize(context);
		
		if (predicate.eval(exchange, boolean.class)) {
			bodyEntryPoint.start(exchange);
		} else {
			context.next(exchange);
		}
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		run(context, exchange);
	}
}
