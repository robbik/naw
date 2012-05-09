package org.naw.executables;

import java.util.List;

import org.naw.core.Engine;
import org.naw.core.task.DataExchange;
import org.naw.core.task.EntryPoint;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.support.TaskContextUtils;

import rk.commons.inject.factory.support.InitializingObject;

public class Executable implements InitializingObject {

	private String name;

	private List<Object> tasks;

	private TaskContext entryPoint;

	public String getExecutableName() {
		return name;
	}

	public void setExecutableName(String name) {
		this.name = name;
	}

	public void setTasks(List<Object> tasks) {
		this.tasks = tasks;
	}

	public TaskContext getEntryPoint() {
		return entryPoint;
	}

	public void initialize() throws Exception {
		if ((tasks == null) || tasks.isEmpty()) {
			throw new IllegalArgumentException("tasks cannot be empty");
		}

		for (int i = 0, n = this.tasks.size(); i < n; ++i) {
			Object o = this.tasks.get(i);

			if (i == 0) {
				if (!(o instanceof EntryPoint)) {
					throw new IllegalArgumentException("first task " + o.getClass() + " must derived from "
							+ EntryPoint.class + " class");
				}
				
				EntryPoint ep = (EntryPoint) o;
				if (!ep.isEntryPoint()) {
					throw new IllegalArgumentException("first task " + o.getClass() + " must be an entry point. " +
							"This is usually can be done by setting entryPoint attribute to true");
				}
			} else {
				if (!(o instanceof Task)) {
					throw new IllegalArgumentException("task " + o.getClass() + " must derived from "
							+ Task.class + " class");
				}
			}
		}
	}
	
	public void attach(Engine engine) {
		if ((tasks == null) || tasks.isEmpty()) {
			throw new IllegalArgumentException("executable already attached");
		}
		
		Task[] tasks = this.tasks.toArray(new Task[0]);

		this.tasks.clear();
		this.tasks = null;
		
		entryPoint = TaskContextUtils.chain(engine, this, tasks);
	}

	public DataExchange createDataExchange() {
		return new DataExchange();
	}
	
	@Override
	public String toString() {
		return name;
	}
}
