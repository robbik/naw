package org.naw.tasks;

import java.util.List;

import org.naw.core.Engine;
import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.Container;
import org.naw.core.task.LifeCycleAware;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskPipeline;
import org.naw.core.task.support.Tasks;
import org.naw.executables.Executable;
import org.naw.expression.Expression;

public class If extends AbstractTask implements LifeCycleAware, Container {

	private Expression predicate;
	
	private List<Task> thenTasks;

	private List<Task> elseTasks;

	private TaskPipeline pthen;

	private TaskPipeline pelse;

	public void setPredicate(Expression predicate) {
		this.predicate = predicate;
	}

	public void setThenTasks(List<Task> thenTasks) {
		this.thenTasks = thenTasks;
	}

	public void setElseTasks(List<Task> elseTasks) {
		this.elseTasks = elseTasks;
	}

	public void beforeAdd(TaskContext ctx) {
		Engine engine = ctx.getPipeline().getEngine();
		Executable executable = ctx.getPipeline().getExecutable();
		
		pthen = Tasks.pipeline(engine, executable, thenTasks).addLast(ctx.getNext());
		pelse = Tasks.pipeline(engine, executable, elseTasks).addLast(ctx.getNext());
		
		thenTasks = null;
		elseTasks = null;
	}

	public TaskContext getTaskContext(String taskId) {
		TaskContext tctx = null;
		
		tctx = pthen.getTaskContext(taskId);
		
		if ((tctx != null) && (pelse != null)) {
			tctx = pelse.getTaskContext(taskId);
		}
		
		return tctx;
	}

	public void run(TaskContext context, MessageExchange exchange) throws Exception {
		if (predicate.eval(exchange, boolean.class)) {
			Tasks.send(pthen, exchange);
		} else if (pelse != null) {
			Tasks.send(pelse, exchange);
		} else {
			context.send(exchange);
		}
	}

	public void recover(TaskContext context, MessageExchange exchange) throws Exception {
		run(context, exchange);
	}
}
