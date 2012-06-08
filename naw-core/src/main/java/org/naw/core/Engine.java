package org.naw.core;

import java.util.Collection;

import org.naw.core.task.TaskQueue;
import org.naw.core.task.support.Timer;
import org.naw.executables.Executable;

public interface Engine {
	
	Timer getTimer();

	TaskQueue getTaskQueue();
	
	Processor createProcessor();
	
	Executable getExecutable(String name);
	
	Collection<Executable> getExecutables();
		
	void start() throws Exception;
	
	void stop();
	
	boolean stopped();
	
	void destroy();
}
