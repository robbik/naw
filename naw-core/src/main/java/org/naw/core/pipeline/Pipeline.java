package org.naw.core.pipeline;

import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.activity.Activity;
import org.naw.core.compensation.CompensationHandler;

public interface Pipeline {

	ProcessContext getProcessContext();

	Sink getSink();

	Pipeline getParent();
	
	Activity getFirstActivity();

	Pipeline init() throws Exception;

	void register(CompensationHandler handler);

	void unregister(CompensationHandler handler);

	void exceptionThrown(Process process, Throwable cause, boolean rethrown);

	Pipeline execute(Process process);

	void hibernate();

	void shutdown();
}
