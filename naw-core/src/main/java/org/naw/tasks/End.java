package org.naw.tasks;

import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.TaskContext;

public class End extends AbstractTask {
	
	public void run(TaskContext context, MessageExchange exchange) throws Exception {
		// do nothing
	}

	public void recover(TaskContext context, MessageExchange exchange) throws Exception {
		// do nothing
	}
	
	@Override
	public String toString() {
		return End.class + " [ executable: " + id.substring(0, id.length() - 4) + " ]";
	}
}
