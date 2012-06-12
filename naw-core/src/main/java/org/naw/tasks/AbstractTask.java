package org.naw.tasks;

import org.naw.core.task.Task;

import rk.commons.inject.annotation.Inject;
import rk.commons.util.ObjectHelper;

public abstract class AbstractTask implements Task {
	
	@Inject
	protected String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return getClass() + " [ id: " + id + " ]";
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		
		if (object instanceof String) {
			return ObjectHelper.equals(id, object);
		} else if (object instanceof Task) {
			return ObjectHelper.equals(id, ((Task) object).getId());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}
}
