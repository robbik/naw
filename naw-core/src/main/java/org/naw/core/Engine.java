package org.naw.core;

import org.naw.core.task.TaskQueue;

public interface Engine {

	TaskQueue getTaskQueue();
	
	Processor createProcessor();

	void start();
	
	void stop();
	
	boolean stopped();
	
	void destroy();
}
