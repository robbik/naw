package org.naw.expression;

import java.util.Map;

import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;

import rk.commons.inject.factory.IocObjectFactory;
import rk.commons.inject.factory.MapWrapper;

public abstract class Expression implements Task {
	
	protected Map<String, Object> objects;
	
	public void setIocObjectFactory(IocObjectFactory iocObjectFactory) {
		objects = new MapWrapper(iocObjectFactory);
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		eval(exchange);
		
		context.next(exchange);
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		run(context, exchange);
	}

	public abstract <T> T eval(DataExchange exchange, Class<? extends T> returnType) throws Exception;
	
	public abstract Object eval(DataExchange exchange) throws Exception;
}
