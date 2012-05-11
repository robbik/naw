package org.naw.tasks;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.naw.core.Engine;
import org.naw.core.task.DataExchange;
import org.naw.core.task.LifeCycleAware;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskPipeline;
import org.naw.core.task.support.Tasks;
import org.naw.executables.Executable;

import rk.commons.inject.factory.support.ObjectQNameAware;

public class Fork implements Task, ObjectQNameAware, LifeCycleAware {

	private List<Task>[] tasks;
	
	private String objectQName;
	
	private TaskPipeline[] pipelines;

	private String joinVariable;
	
	@SuppressWarnings("unchecked")
	public void setFlowTasks(Object[] tasks) {
		this.tasks = new List[tasks.length];
		System.arraycopy(tasks, 0, this.tasks, 0, tasks.length);
	}

	public void setObjectQName(String objectQName) {
		this.objectQName = objectQName;
	}

	public void beforeAdd(TaskContext ctx) {
		pipelines = new TaskPipeline[tasks.length];
		
		Engine engine = ctx.getPipeline().getEngine();
		Executable executable = ctx.getPipeline().getExecutable();
		
		for (int i = 0, n = tasks.length; i < n; ++i) {
			pipelines[i] = Tasks.pipeline(engine, executable, tasks[i]).addLast(ctx);
		}
		
		joinVariable = objectQName + "___join";
		
		tasks = null;
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		AtomicInteger join = exchange.getpriv(joinVariable);
		
		if (join == null) {
			int n = pipelines.length;

			exchange.setpriv(joinVariable, new AtomicInteger(n));
			
			for (int i = 0; i < n; ++i) {
				pipelines[i].start(exchange);
			}
		} else {
			int counter = join.decrementAndGet();
			
			if (counter <= 0) {
				exchange.unsetpriv(joinVariable);

				context.forward(exchange);
			}
		}
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		run(context, exchange);
	}
}
