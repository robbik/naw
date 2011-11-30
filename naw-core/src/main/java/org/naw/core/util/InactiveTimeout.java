package org.naw.core.util;

import java.io.Serializable;

public class InactiveTimeout implements Timeout, Serializable {

	private static final long serialVersionUID = -4123019560857598185L;

	private String processId;

	private String activityName;

	private long deadline;

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getProcessId() {
		return processId;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}

	public long getDeadline() {
		return deadline;
	}

	public Timer getTimer() {
		return null;
	}

	public TimerTask getTask() {
		return null;
	}

	public boolean isExpired() {
		return false;
	}

	public boolean isCanceled() {
		return false;
	}

	public void cancel() {
		// do nothing
	}
	
	public static InactiveTimeout copyFrom(Timeout timeout) {
		InactiveTimeout me = new InactiveTimeout();
		
		me.processId = timeout.getProcessId();
		me.activityName = timeout.getActivityName();
		me.deadline = timeout.getDeadline();
		
		return me;
	}
}
