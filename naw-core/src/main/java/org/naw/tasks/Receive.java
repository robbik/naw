package org.naw.tasks;

import java.util.Calendar;
import java.util.List;

import org.apache.axis.types.DateTime;
import org.apache.axis.types.Duration;
import org.naw.core.task.DataExchange;
import org.naw.core.task.EntryPoint;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskContextFuture;
import org.naw.core.task.support.TaskContextUtils;
import org.naw.links.Link;

import rk.commons.ioc.factory.support.InitializingObject;
import rk.commons.ioc.factory.support.ObjectQNameAware;
import rk.commons.util.ObjectUtils;

public class Receive implements EntryPoint, ObjectQNameAware, InitializingObject {
	
	private Link link;

	private String varName;

	private long deadline;

	private Duration duration;
	
	private List<Task> onReceiveTasks;
	
	private List<Task> onTimeoutTasks;
	
	private boolean entryPoint;
	
	private String exchangeVarName;
	
	private String objectQName;

	private String timeoutVarName;
	
	private TaskContext onTimeoutChain;
	
	private TaskContext onReceiveChain;

	public void setPartnerLink(Link partnerLink) {
		this.link = partnerLink;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}
	
	public void setDeadline(DateTime deadline) {
		this.deadline = deadline.getCalendar().getTimeInMillis();
	}
	
	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public void setOnReceiveTasks(List<Task> onReceiveTasks) {
		this.onReceiveTasks = onReceiveTasks;
	}

	public void setOnTimeoutTasks(List<Task> onTimeoutTasks) {
		this.onTimeoutTasks = onTimeoutTasks;
	}
	
	public void setEntryPoint(boolean entryPoint) {
		this.entryPoint = entryPoint;
	}

	public boolean isEntryPoint() {
		return entryPoint;
	}

	public void setExchangeVarName(String exchangeVarName) {
		this.exchangeVarName = exchangeVarName;
	}

	public void setObjectQName(String objectQName) {
		this.objectQName = objectQName;
		
		timeoutVarName = objectQName.concat("__timeout");
	}

	public void initialize() throws Exception {
		if (exchangeVarName != null) {
			exchangeVarName = ObjectUtils.getPackageName(objectQName).concat(
					":sendAndReceive__exchange#").concat(exchangeVarName);
		}
	}

	private synchronized void initialize(TaskContext context) throws Exception {
		if (onReceiveChain != null) {
			return;
		}
		
		TaskContext nextTaskContext = context.getNextTaskContext();
		
		onReceiveChain = TaskContextUtils.chain(context.getEngine(), context.getExecutable(), onReceiveTasks);
		onReceiveTasks = null;
		
		if (nextTaskContext != null) {
			TaskContextUtils.addLast(onReceiveChain, nextTaskContext);
		}
		
		if (onTimeoutTasks != null) {
			onTimeoutChain = TaskContextUtils.chain(context.getEngine(), context.getExecutable(), onTimeoutTasks);
			onTimeoutTasks = null;
			
			if (nextTaskContext != null) {
				TaskContextUtils.addLast(onTimeoutChain, nextTaskContext);
			}
		}
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		// initialize it once
		initialize(context);
		
		// receive data
		Object correlation;
		
		if (exchangeVarName == null) {
			correlation = null;
		} else {
			correlation = exchange.getpriv(exchangeVarName);
			
			if (correlation == null) {
				throw new NullPointerException("no exchange value in variable " + exchangeVarName);
			}
		}
		
		Object data = link.receive(correlation);

		// access scheduled onTimeout
		TaskContextFuture future;
		
		if (entryPoint) {
			future = null;
		} else {
			future = exchange.getpriv(timeoutVarName);
		}

		if (data == null) {
			if ((onTimeoutChain != null) && (future == null)) {
				long deadline;
				
				if (duration == null) {
					deadline = this.deadline;
				} else {
					deadline = duration.add(Calendar.getInstance()).getTimeInMillis();
				}

				// schedule to start onTimeout
				future = onTimeoutChain.startLater(exchange, deadline);

				// save it for further access
				if (!entryPoint || (exchange == null)) {
					exchange.setpriv(timeoutVarName, future);
				}
			}

			// we need to check the data again so run this task again
			context.start(exchange);
		} else {
			if (future != null) {
				// cancel onTimeout schedule
				future.cancel();

				// prevent it from further access
				exchange.unsetpriv(timeoutVarName);
			}
			
			if (entryPoint) {
				// this is entry point, so we need to re-run this task for another receive
				context.start(null);

				// create new data exchange
				exchange = context.getExecutable().createDataExchange();
			}

			// save received data
			exchange.set(varName, data);
			
			// unset correlation data
			if (exchangeVarName != null) {
				exchange.unsetpriv(exchangeVarName);
			}

			// start onReceive part
			onReceiveChain.start(exchange);
		}
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		run(context, exchange);
	}
}
