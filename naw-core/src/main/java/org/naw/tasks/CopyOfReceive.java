package org.naw.tasks;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.axis.types.Duration;
import org.naw.core.Engine;
import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskContextFuture;
import org.naw.core.task.support.TaskContextUtils;
import org.naw.engine.NawProcess;
import org.naw.executables.Executable;
import org.naw.links.Link;

import rk.commons.ioc.factory.support.ObjectQNameAware;
import rk.commons.logging.Logger;
import rk.commons.logging.LoggerFactory;

public class CopyOfReceive implements Task, ObjectQNameAware {
	
	private final String ivarRefNo;
	
	private Link link;

	private String varName;
	
	private List<Task> onReceiveTasks;
	
	private List<Task> onTimeoutTasks;

	private long deadline;

	private Duration duration;

	private String timeoutVarName;
	
	private TaskContext onTimeoutChain;
	
	private TaskContext onReceiveChain;

	private Logger log;

	public CopyOfReceive() {
		this.ivarRefNo = null;
	}

	public CopyOfReceive(String varRefNo) {
		this.ivarRefNo = varRefNo;
	}

	public void setPartnerLink(Link partnerLink) {
		this.link = partnerLink;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public void setOnReceiveTasks(List<Task> onReceiveTasks) {
		this.onReceiveTasks = onReceiveTasks;
	}

	public void setOnTimeoutTasks(List<Task> onTimeoutTasks) {
		this.onTimeoutTasks = onTimeoutTasks;
	}

	public void setObjectQName(String objectQName) {
		timeoutVarName = objectQName.concat("__timeout");
		
		log = LoggerFactory.getLogger(objectQName);
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

	public void execute(NawProcess process, CompletionHandler completion)
			throws Exception {
		Object data;

		if (ivarRefNo == null) {
			data = link.receive();
		} else {
			String refNo = (String) process.geti(ivarRefNo);

			if (refNo == null) {
				log.warning("variable ".concat(ivarRefNo).concat(
						" has null value which referenced by receive activity"));

				data = null;
			} else {
				data = link.receive(refNo);
			}
		}

		if (data == null) {
			if (onTimeout == null) {
				reexecuteThis(process, completion, NEXT_RECEIVE_DELAY,
						TimeUnit.MILLISECONDS);
			} else {
				long nextReceiveDelay = getTimeOutDuration(process);

				if (nextReceiveDelay > 0) {
					if (nextReceiveDelay > NEXT_RECEIVE_DELAY) {
						reexecuteThis(process, completion, NEXT_RECEIVE_DELAY,
								TimeUnit.MILLISECONDS);
					} else {
						reexecuteThis(process, completion, nextReceiveDelay,
								TimeUnit.MILLISECONDS);
					}
				} else {
					execute(onTimeout, process, completion);
				}
			}
		} else {
			process.set(varName, data);

			execute(onReceive, process, completion);
		}
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		initialize(context);
		
		Object data;
		
		if (ivarRefNo == null) {
			data = link.receive();
		} else {
			String refNo = (String) exchange.getpriv(ivarRefNo);
			
			if (refNo == null) {
				log.warning("variable " + ivarRefNo + " has null value which referenced by receive task");

				data = null;
			} else {
				data = link.receive(refNo);
			}
		}

		TaskContextFuture future = exchange.getpriv(timeoutVarName);

		if (data == null) {
			if (ontimeout == null) {
				context.start(exchange);
			} else if (future == null) {
				if (duration == null) {
					ontimeout.startLater(exchange, deadline);
				} else {
					deadline = duration.add(Calendar.getInstance()).getTimeInMillis();

					ontimeout.startLater(exchange, deadline);
				}

				exchange.setpriv(timeoutVarName, future);

				context.start(exchange);
			} else {
				long now = System.currentTimeMillis();

				if (now >= deadline) {
					future.cancel();
					
					exchange.
				}
			}
		} else {
			if (future != null) {
				future.cancel();
				exchange.unsetpriv(timeoutVarName);
			}

			exchange.set(varName, data);

			onreceive.start(exchange);
		}
	}

	public void recover(TaskContext context, DataExchange exchange)
			throws Exception {
		run(context, exchange);
	}
}
