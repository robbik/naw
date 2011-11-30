package org.naw.core;

import org.naw.core.activity.Activity;

public interface ProcessLifeCycleListener {

	void processStateChange(ProcessContext ctx, Process process,
			ProcessState newState, Activity newActivity);

	void processBeginWait(ProcessContext ctx, Process process, Activity activity);
	
	void processEndWait(ProcessContext ctx, Process process, Activity activity);
}
