package org.naw.tasks;

import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;

import rk.commons.logging.Logger;
import rk.commons.logging.LoggerFactory;

public class Log implements Task {
	
	private Logger log;
	
	private String text;
	
	public void setLogName(String logName) {
		log = LoggerFactory.getLogger(logName);
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		log.info(text);
		
		context.next(exchange);
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		run(context, exchange);
	}
}
