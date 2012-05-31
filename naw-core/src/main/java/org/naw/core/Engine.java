package org.naw.core;

import org.naw.core.task.TaskQueue;
import org.naw.core.task.support.Timer;

public interface Engine {
	
	Timer getTimer();

	TaskQueue getTaskQueue();
	
	Storage getStorage();
	
	Processor createProcessor();
		
	void start() throws Exception;
	
	void stop();
	
	boolean stopped();
	
	void destroy();
}
