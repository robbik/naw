package org.naw.core;

import org.naw.core.activity.Activity;

public interface ProcessLifeCycleListener {
	
	public static final String STATE_CHANGE = "STATE_CHANGE";
	
	public static final String BEGIN_WAIT = "BEGIN_WAIT";
	
	public static final String END_WAIT = "END_WAIT";

	void processStateChange(ProcessContext ctx, Process process,
			ProcessState newState, Activity newActivity);

	void processBeginWait(ProcessContext ctx, Process process, Activity activity);
	
	void processEndWait(ProcessContext ctx, Process process, Activity activity);
}
