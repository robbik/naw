package org.naw.tasks;

import java.util.Calendar;
import java.util.List;

import org.apache.axis.types.DateTime;
import org.apache.axis.types.Duration;
import org.naw.core.Engine;
import org.naw.core.task.DataExchange;
import org.naw.core.task.LifeCycleAware;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.TaskPipeline;
import org.naw.core.task.support.Tasks;
import org.naw.exceptions.LinkException;
import org.naw.executables.Executable;
import org.naw.links.AsyncCallback;
import org.naw.links.AsyncResult;
import org.naw.links.LinkExchange;
import org.naw.links.Message;

public class ReceiveReply implements Task, LifeCycleAware, AsyncCallback<Message> {

	private String variable;

	private long deadline;

	private Duration duration;
	
	private String exchangeVariable;
	
	private List<Task> receiveTasks;
	
	private List<Task> errorTasks;
	
	private List<Task> timeoutTasks;
	
	private TaskPipeline receivePipeline;
	
	private TaskPipeline errorPipeline;
	
	private TaskPipeline timeoutPipeline;

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public void setExchangeVariable(String exchangeVariable) {
		this.exchangeVariable = exchangeVariable;
	}
	
	public void setDeadline(DateTime deadline) {
		this.deadline = deadline.getCalendar().getTimeInMillis();
	}
	
	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public void setReceiveTasks(List<Task> receiveTasks) {
		this.receiveTasks = receiveTasks;
	}

	public void setErrorTasks(List<Task> errorTasks) {
		this.errorTasks = errorTasks;
	}

	public void setTimeoutTasks(List<Task> timeoutTasks) {
		this.timeoutTasks = timeoutTasks;
	}

	public void beforeAdd(TaskContext ctx) {
		Engine engine = ctx.getPipeline().getEngine();
		Executable executable = ctx.getPipeline().getExecutable();
		
		TaskContext next = ctx.getNext();
		
		receivePipeline = Tasks.pipeline(engine, executable, receiveTasks).addLast(next);
		
		if (errorTasks != null) {
			errorPipeline = Tasks.pipeline(engine, executable, errorTasks).addLast(next);
		}
		
		if (timeoutTasks != null) {
			timeoutPipeline = Tasks.pipeline(engine, executable, timeoutTasks).addLast(next);
		}
		
		receiveTasks = null;
		errorTasks = null;
		timeoutTasks = null;
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		// calculate timeout deadline
		long deadline;
		
		if (timeoutPipeline == null) {
			deadline = -1;
		} else {
			if (duration == null) {
				deadline = this.deadline;
			} else {
				deadline = duration.add(Calendar.getInstance()).getTimeInMillis();
			}
		}
		
		// use correlation
		LinkExchange lex = exchange.getpriv(exchangeVariable);
		
		if (lex == null) {
			// start timeout tasks
			
			if (timeoutPipeline == null) {
				return; // DEAD END
			} else {
				timeoutPipeline.start(exchange);
			}
		}
		
		// begin receive data
		boolean error = false;
		
		AsyncAttachment attachment = new AsyncAttachment();
		attachment.exchange = exchange;
		attachment.context = context;
		
		try {
			lex.getLink().asyncReceiveReply(lex.getCorrelation(), attachment, deadline, this);
		} catch (LinkException e) {
			// set last error
			exchange.setLastError(e.getErrorCode(), e.getMessage());
			
			error = true;
		}
		
		// error handling
		if (error && (errorPipeline != null)) {
			// start error tasks
			errorPipeline.start(exchange);
		} 
	}
	
	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		run(context, exchange);
	}

	public void completed(AsyncResult<Message> asyncResult) {
		AsyncAttachment attachment = (AsyncAttachment) asyncResult.getAttachment();
		
		if (asyncResult.isSuccess()) {
			// received
			DataExchange exchange = attachment.exchange;
			
			// set received data to variable
			exchange.set(variable, asyncResult.getResult().getBody());
			
			// unset correlation
			if (exchangeVariable != null) {
				exchange.unsetpriv(exchangeVariable);
			}
			
			// start receive tasks
			receivePipeline.start(exchange);
		} else if (!asyncResult.isCancelled()) {
			Throwable cause = asyncResult.getCause();
			
			// error handling
			if (cause instanceof LinkException) {
				int errorCode = ((LinkException) cause).getErrorCode();

				// set last error
				attachment.exchange.setLastError(errorCode, ((LinkException) cause).getMessage());
				
				// start error tasks
				if ((errorCode != 0) && (errorPipeline != null)) {
					errorPipeline.start(attachment.exchange);
				}
			}
		}
	}
	
	public void timeout(AsyncResult<Message> asyncResult) {
		// start timeout tasks
		timeoutPipeline.start(((AsyncAttachment) asyncResult.getAttachment()).exchange);
	}
	
	class AsyncAttachment {
		TaskContext context;
		
		DataExchange exchange;
	}
}
