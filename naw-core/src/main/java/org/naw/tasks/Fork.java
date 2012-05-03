package org.naw.tasks;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.support.TaskContextUtils;

import rk.commons.ioc.factory.support.ObjectQNameAware;

public class Fork implements Task, ObjectQNameAware {

	private List<Task>[] flowTasks;
	
	private String objectQName;
	
	private TaskContext[] flowEntryPoints;

	private String privVarName;
	
	@SuppressWarnings("unchecked")
	public void setFlowTasks(Object[] flowTasks) {
		this.flowTasks = new List[flowTasks.length];
		System.arraycopy(flowTasks, 0, this.flowTasks, 0, flowTasks.length);
	}

	public void setObjectQName(String objectQName) {
		this.objectQName = objectQName;
	}

	private synchronized void initialize(TaskContext context) {
		if (flowEntryPoints != null) {
			return;
		}
		
		flowEntryPoints = new TaskContext[flowTasks.length];
		
		for (int i = 0, n = flowTasks.length; i < n; ++i) {
			flowEntryPoints[i] = TaskContextUtils.getFirstTaskContext(context, flowTasks[i]);
			flowTasks[i] = null;

			TaskContextUtils.addLast(flowEntryPoints[i], context);
		}
		
		flowTasks = null;
		
		privVarName = objectQName + "___joinCounter";
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		initialize(context);
		
		AtomicInteger joinCounter = exchange.getpriv(privVarName);
		
		if (joinCounter == null) {
			int n = flowEntryPoints.length;

			exchange.setpriv(privVarName, new AtomicInteger(n));
			
			for (int i = 0; i < n; ++i) {
				flowEntryPoints[i].start(exchange);
			}
		} else {
			int counter = joinCounter.decrementAndGet();
			
			if (counter <= 0) {
				exchange.remove(privVarName);

				context.next(exchange);
			}
		}
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		run(context, exchange);
	}
}
