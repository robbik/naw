package org.naw.tasks;

import java.util.List;

import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.support.TaskContextUtils;
import org.naw.expression.Expression;

public class If implements Task {

	private Expression predicate;
	
	private List<Task> thenTasks;

	private List<Task> elseTasks;

	private volatile TaskContext thenEntryPoint;

	private volatile TaskContext elseEntryPoint;

	public void setPredicate(Expression predicate) {
		this.predicate = predicate;
	}

	public void setThenTasks(List<Task> thenTasks) {
		this.thenTasks = thenTasks;
	}

	public void setElseTasks(List<Task> elseTasks) {
		this.elseTasks = elseTasks;
	}

	private synchronized void initialize(TaskContext context) {
		if (thenEntryPoint == null) {
			thenEntryPoint = TaskContextUtils.getFirstTaskContext(context, thenTasks);
			thenTasks = null;

			TaskContextUtils.addLast(thenEntryPoint, context);
		}

		if ((elseEntryPoint == null) && (elseTasks != null)) {
			elseEntryPoint = TaskContextUtils.getFirstTaskContext(context, elseTasks);
			elseTasks = null;

			TaskContextUtils.addLast(elseEntryPoint, context);
		}
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		initialize(context);
		
		if (predicate.eval(exchange, boolean.class)) {
			thenEntryPoint.start(exchange);
		} else if (elseEntryPoint != null) {
			elseEntryPoint.start(exchange);
		} else {
			context.next(exchange);
		}
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		run(context, exchange);
	}
}
