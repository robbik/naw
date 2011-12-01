package org.naw.core.listener;

import org.naw.core.Process;
import org.naw.core.ProcessContext;

public class SimpleLifeCycleListener implements LifeCycleListener {

	public void processCreated(ProcessContext ctx, Process process) {
		// do nothing
	}

	public void processStateChange(ProcessContext ctx, Process process) {
		// do nothing
	}

	public void processTerminated(ProcessContext ctx, Process process) {
		// do nothing
	}

	public void processContextInitialized(ProcessContext ctx) {
		// do nothing
	}

	public void processContextDestroyed(ProcessContext ctx) {
		// do nothing
	}
}
