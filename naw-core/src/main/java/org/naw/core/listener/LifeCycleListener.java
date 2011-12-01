package org.naw.core.listener;

import org.naw.core.Process;
import org.naw.core.ProcessContext;

public interface LifeCycleListener {
	
	void processCreated(ProcessContext ctx, Process process);

	void processStateChange(ProcessContext ctx, Process process);
	
	void processTerminated(ProcessContext ctx, Process process);
	
	void processContextInitialized(ProcessContext ctx);
	
	void processContextDestroyed(ProcessContext ctx);
	
	public static enum Category {
		PROCESS_STATE_CHANGE,
		PROCESS_CREATED,
		PROCESS_TERMINATED,
		PROCESS_CONTEXT_INITIALIZED,
		PROCESS_CONTEXT_DESTROYED
	}
}
