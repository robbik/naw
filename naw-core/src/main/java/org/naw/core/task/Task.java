package org.naw.core.task;

public interface Task {

	void run(TaskContext context, DataExchange exchange) throws Exception;
	
	void recover(TaskContext context, DataExchange exchange) throws Exception;
}
