package org.naw.executables;

import java.util.ArrayList;
import java.util.List;

import org.naw.core.Engine;
import org.naw.core.Storage;
import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskPipeline;
import org.naw.core.task.support.Tasks;
import org.naw.tasks.EndOfExecutable;

public class Executable {
	
	private String name;

	private List<Object> tasks;
	
	private boolean inMemory;
	
	private TaskPipeline pipeline;
	
	private Engine engine;
	
	private Storage storage;

	public String getName() {
		return name;
	}

	public void setExecutableName(String name) {
		this.name = name;
	}

	public void setTasks(List<Object> tasks) {
		this.tasks = tasks;
	}
	
	public void setInMemory(boolean inMemory) {
		this.inMemory = inMemory;
	}
	
	public boolean isInMemory() {
		return inMemory;
	}
	
	public TaskPipeline getPipeline() {
		return pipeline;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void initialize(Engine engine) throws Exception {
		this.engine = engine;
		
		storage = engine.getStorage();
		
		if ((tasks == null) || tasks.isEmpty()) {
			throw new IllegalArgumentException("tasks cannot be empty");
		}

		for (int i = 0, n = tasks.size(); i < n; ++i) {
			Object o = tasks.get(i);

			if (!(o instanceof Task)) {
				throw new IllegalArgumentException("task " + o.getClass() + " must derived from " + Task.class + " class");
			}
		}
		
		EndOfExecutable task = new EndOfExecutable();
		task.setObjectQName(name + "#end");
		
		tasks = new ArrayList<Object>(this.tasks);
		tasks.add(task);

		pipeline = Tasks.pipeline(engine, this, (List) tasks);
	}
	
	public void start() {
		start(null);
	}

	public void start(MessageExchange exchange) {
		Tasks.send(pipeline, exchange);
	}
	
	public MessageExchange createMessageExchange() {
		return storage.createMessageExchange(engine, this);
	}
	
	@Override
	public String toString() {
		return Executable.class + " [ name: " + name + "; transient: " + inMemory + " ]";
	}
}
