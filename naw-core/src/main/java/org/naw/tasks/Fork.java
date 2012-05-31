package org.naw.tasks;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.naw.core.Engine;
import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.LifeCycleAware;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskPipeline;
import org.naw.core.task.support.Tasks;
import org.naw.executables.Executable;

public class Fork extends AbstractTask implements LifeCycleAware {

	private List<Task>[] tasks;
	
	private TaskPipeline[] pipelines;

	private String joinVariable;
	
	@SuppressWarnings("unchecked")
	public void setFlowTasks(Object[] tasks) {
		this.tasks = new List[tasks.length];
		System.arraycopy(tasks, 0, this.tasks, 0, tasks.length);
	}

	public void beforeAdd(TaskContext ctx) {
		pipelines = new TaskPipeline[tasks.length];
		
		Engine engine = ctx.getPipeline().getEngine();
		Executable executable = ctx.getPipeline().getExecutable();
		
		for (int i = 0, n = tasks.length; i < n; ++i) {
			pipelines[i] = Tasks.pipeline(engine, executable, tasks[i]).addLast(ctx);
		}
		
		joinVariable = id.concat("___join");
		
		tasks = null;
	}

	public void run(TaskContext context, MessageExchange exchange) throws Exception {
		AtomicInteger join = exchange.getpriv(joinVariable);
		
		if (join == null) {
			int n = pipelines.length;

			exchange.setpriv(joinVariable, new AtomicInteger(n));
			
			for (int i = 0; i < n; ++i) {
				Tasks.send(pipelines[i], exchange);
			}
		} else {
			int counter = join.decrementAndGet();
			
			if (counter <= 0) {
				exchange.unsetpriv(joinVariable);

				context.send(exchange);
			}
		}
	}

	public void recover(TaskContext context, MessageExchange exchange) throws Exception {
		run(context, exchange);
	}
}
