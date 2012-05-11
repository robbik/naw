package org.naw.core;

import org.naw.core.task.TaskQueue;
import org.naw.core.task.support.Timer;

public interface Engine {
	
	Timer getTimer();

	TaskQueue getTaskQueue();
	
	Processor createProcessor();

	void start();
	
	void stop();
	
	boolean stopped();
	
	void destroy();
}
