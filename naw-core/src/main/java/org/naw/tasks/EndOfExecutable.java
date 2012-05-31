package org.naw.tasks;

import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.TaskContext;

public class EndOfExecutable extends AbstractTask {
	
	public void run(TaskContext context, MessageExchange exchange) throws Exception {
		exchange.destroy();
	}

	public void recover(TaskContext context, MessageExchange exchange) throws Exception {
		run(context, exchange);
	}
	
	@Override
	public String toString() {
		return EndOfExecutable.class + " [ executable: " + id.substring(0, id.length() - 4) + " ]";
	}
}
