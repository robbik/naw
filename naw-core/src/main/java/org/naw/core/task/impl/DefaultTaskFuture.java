package org.naw.core.task.impl;

import org.naw.core.Storage;
import org.naw.core.task.TaskFuture;

public class DefaultTaskFuture implements TaskFuture {
	
	private final Storage storage;
	
	private final String mexId;
	
	private final String taskId;
	
	public DefaultTaskFuture(Storage storage, String mexId, String taskId) {
		this.storage = storage;
		
		this.mexId = mexId;
		this.taskId = taskId;
	}
	
	public void beforeAdd() {
		if (mexId != null) {
			storage.addTask(mexId, taskId);
		}
	}
	
	public void beforeRun() {
		if (mexId != null) {
			storage.addOnGoingTask(mexId, taskId);
		}
	}

	public void cancel() {
		if (mexId != null) {
			storage.removeTask(mexId, taskId);
		}
	}

	public void setSuccess() {
		if (mexId != null) {
			storage.removeTask(mexId, taskId);
		}
	}

	public void setFailure(Throwable cause) {
		if (mexId != null) {
			storage.removeTask(mexId, taskId);
		}
	}

}
