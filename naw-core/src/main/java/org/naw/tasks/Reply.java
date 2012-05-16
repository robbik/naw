package org.naw.tasks;

import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.exceptions.LinkException;
import org.naw.links.Link;
import org.naw.links.LinkExchange;
import org.naw.links.Message;

import rk.commons.inject.factory.support.ObjectQNameAware;
import rk.commons.logging.LoggerFactory;

public class Reply implements Task, ObjectQNameAware {

	private Link to;

	private String variable;

	private boolean retriable;
	
	private String exchangeVariable;

	private String objectQName;

	public void setTo(Link to) {
		this.to = to;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public void setRetriable(boolean retriable) {
		this.retriable = retriable;
	}
	
	public void setExchangeVariable(String exchangeVariable) {
		this.exchangeVariable = exchangeVariable;
	}

	public void setObjectQName(String objectQName) {
		this.objectQName = objectQName;
	}
	
	private void dosend(DataExchange exchange) throws Exception {
		int errorCode = 0;
		String errorMsg = "No error";
		
		LinkExchange lex = exchange.getpriv(exchangeVariable);
		
		if (lex != null) {
			Link to = this.to;
			
			if (to == null) {
				to = lex.getLink();
			}
			
			try {
				to.sendReply(new Message(lex.getCorrelation(), exchange.get(variable)));
			} catch (LinkException e) {
				errorCode = e.getErrorCode();
				errorMsg = e.getMessage();
			}
		}
		
		exchange.setLastError(errorCode, errorMsg);
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		dosend(exchange);
		
		context.forward(exchange);
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		if (retriable) {
			dosend(exchange);
		} else {
			LoggerFactory.getLogger(objectQName).warning("activity is recovered but retriable is false");
			
			exchange.setLastError(0, "No error");
		}

		context.forward(exchange);
	}
}
