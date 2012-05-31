package org.naw.tasks;

import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.TaskContext;
import org.naw.expression.Expression;
import org.naw.links.LinkExchange;

public class Correlation extends AbstractTask {

	private String exchangeVariable;
	
	private Expression value;

	public void setExchangeVariable(String exchangeVariable) {
		this.exchangeVariable = exchangeVariable;
	}

	public void setValue(Expression value) {
		this.value = value;
	}

	public void run(TaskContext context, MessageExchange exchange) throws Exception {
		exchange.setpriv(exchangeVariable, new LinkExchange(value.eval(exchange), null));
		
		context.send(exchange);
	}

	public void recover(TaskContext context, MessageExchange exchange) throws Exception {
		run(context, exchange);
	}
}
