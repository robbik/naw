package org.naw.executables;

import java.util.List;

import org.naw.core.Engine;
import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.support.Tasks;

import rk.commons.inject.factory.support.InitializingObject;

public class Executable implements InitializingObject {
	
	private String name;

	private List<Object> tasks;

	public String getExecutableName() {
		return name;
	}

	public void setExecutableName(String name) {
		this.name = name;
	}

	public void setTasks(List<Object> tasks) {
		this.tasks = tasks;
	}

	public void initialize() throws Exception {
		if ((tasks == null) || tasks.isEmpty()) {
			throw new IllegalArgumentException("tasks cannot be empty");
		}

		for (int i = 0, n = this.tasks.size(); i < n; ++i) {
			Object o = this.tasks.get(i);

			if (!(o instanceof Task)) {
				throw new IllegalArgumentException("task " + o.getClass() + " must derived from " + Task.class + " class");
			}
		}
	}
	
	public void start(Engine engine) {
		start(engine, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void start(Engine engine, DataExchange exchange) {
		Tasks.pipeline(engine, this, (List) tasks).start(exchange);
	}

	public DataExchange createDataExchange() {
		return new DataExchange();
	}
	
	@Override
	public String toString() {
		return name;
	}
}
