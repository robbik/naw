package org.naw.core.task;

import java.util.concurrent.TimeUnit;

import org.naw.core.Engine;
import org.naw.executables.Executable;

public interface TaskContext {

	Engine getEngine();

	Executable getExecutable();

	Task getTask();
	
	TaskContext getNextTaskContext();

	void next(DataExchange exchange);
	
	TaskContextFuture nextLater(DataExchange exchange, long delay, TimeUnit unit);

	TaskContextFuture nextLater(DataExchange exchange, long deadline);

	void start(DataExchange exchange);
	
	TaskContextFuture startLater(DataExchange exchange, long delay, TimeUnit unit);
	
	TaskContextFuture startLater(DataExchange exchange, long deadline);
	
	void run(DataExchange exchange) throws Exception;
}
