package org.naw.core.util.internal;

import java.util.Collection;

import org.naw.core.Process;

public abstract class ProcessUtils {

	public static int countRunning(Collection<Process> processes) {
		int running = 0;

		for (Process proc : processes) {
			switch (proc.getState()) {
			case INIT:
			case HIBERNATED:
			case TERMINATED:
			case SLEEP:
				break;
			default:
				++running;
				break;
			}
		}

		return running;
	}

	public static int countSleeping(Collection<Process> processes) {
		int sleeping = 0;

		for (Process proc : processes) {
			switch (proc.getState()) {
			case SLEEP:
				++sleeping;
				break;
			default:
				break;
			}
		}

		return sleeping;
	}
}
