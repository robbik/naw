package org.naw.tasks;

import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.TaskContext;
import org.naw.exceptions.LinkException;
import org.naw.links.Link;
import org.naw.links.LinkExchange;
import org.naw.links.Message;

import rk.commons.logging.LoggerFactory;

public class Reply extends AbstractTask {

	private Link to;

	private String variable;

	private boolean retriable;
	
	private String exchangeVariable;

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
	
	private void dosend(MessageExchange exchange) throws Exception {
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

	public void run(TaskContext context, MessageExchange exchange) throws Exception {
		dosend(exchange);
		
		context.send(exchange);
	}

	public void recover(TaskContext context, MessageExchange exchange) throws Exception {
		if (retriable) {
			dosend(exchange);
		} else {
			LoggerFactory.getLogger(id).warning("activity is recovered but retriable is false");
			
			exchange.setLastError(0, "No error");
		}

		context.send(exchange);
	}
	
	@Override
	public String toString() {
		return Reply.class + " [ id: " + id + "; variable: " + variable + "; exchangeVariable: " + exchangeVariable + " ]";
	}
}
