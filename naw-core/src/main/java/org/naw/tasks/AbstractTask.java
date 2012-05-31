package org.naw.tasks;

import org.naw.core.task.Task;

import rk.commons.inject.factory.support.ObjectQNameAware;

public abstract class AbstractTask implements Task, ObjectQNameAware {
	
	protected String id;

	public String getId() {
		return id;
	}

	public void setObjectQName(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return getClass() + " [ id: " + id + " ]";
	}
}
