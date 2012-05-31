package org.naw.tasks;

import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.TaskContext;

import rk.commons.logging.Logger;
import rk.commons.logging.LoggerFactory;

public class Log extends AbstractTask {
	
	private Logger log;
	
	private String text;
	
	public void setLogName(String logName) {
		log = LoggerFactory.getLogger(logName);
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public void run(TaskContext context, MessageExchange exchange) throws Exception {
		log.info(text);
		
		context.send(exchange);
	}

	public void recover(TaskContext context, MessageExchange exchange) throws Exception {
		run(context, exchange);
	}
}
