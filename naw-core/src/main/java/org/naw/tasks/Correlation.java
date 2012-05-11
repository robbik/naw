package org.naw.tasks;

import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.expression.Expression;
import org.naw.links.LinkExchange;

public class Correlation implements Task {

	private String exchangeVariable;
	
	private Expression value;

	public void setExchangeVariable(String exchangeVariable) {
		this.exchangeVariable = exchangeVariable;
	}

	public void setValue(Expression value) {
		this.value = value;
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		exchange.setpriv(exchangeVariable, new LinkExchange(value.eval(exchange), null));
		
		context.forward(exchange);
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		run(context, exchange);
	}
}
