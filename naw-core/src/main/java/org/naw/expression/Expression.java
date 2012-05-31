package org.naw.expression;

import java.util.Map;

import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.TaskContext;
import org.naw.tasks.AbstractTask;

import rk.commons.inject.factory.MapWrapper;
import rk.commons.inject.factory.ObjectFactory;

public abstract class Expression extends AbstractTask {
	
	protected Map<String, Object> objects;
	
	public void setObjectFactory(ObjectFactory factory) {
		objects = new MapWrapper(factory);
	}

	public void run(TaskContext context, MessageExchange exchange) throws Exception {
		eval(exchange);
		
		context.send(exchange);
	}

	public void recover(TaskContext context, MessageExchange exchange) throws Exception {
		run(context, exchange);
	}

	public abstract <T> T eval(MessageExchange exchange, Class<? extends T> returnType) throws Exception;
	
	public abstract Object eval(MessageExchange exchange) throws Exception;
}
