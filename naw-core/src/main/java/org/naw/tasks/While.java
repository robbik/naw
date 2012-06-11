package org.naw.tasks;

import java.util.List;

import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.LifeCycleAware;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskPipeline;
import org.naw.core.task.support.Tasks;
import org.naw.expression.Expression;

public class While extends AbstractTask implements LifeCycleAware {

	private Expression predicate;

	private List<Task> tasks;
	
	private TaskPipeline pipeline;

	public void setPredicate(Expression predicate) {
		this.predicate = predicate;
	}

	public void setBodyTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public void beforeAdd(TaskContext ctx) {
		pipeline = Tasks.pipeline(ctx.getPipeline().getEngine(), ctx.getPipeline().getExecutable(), tasks).addLast(ctx);
		
		tasks = null;
	}

	public TaskContext getTaskContext(String taskId) {
		return pipeline.getTaskContext(taskId);
	}

	public void run(TaskContext context, MessageExchange exchange) throws Exception {
		if (predicate.eval(exchange, boolean.class)) {
			Tasks.send(pipeline, exchange);
		} else {
			context.send(exchange);
		}
	}

	public void recover(TaskContext context, MessageExchange exchange) throws Exception {
		run(context, exchange);
	}
}
