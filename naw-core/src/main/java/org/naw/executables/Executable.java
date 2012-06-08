package org.naw.executables;

import java.util.ArrayList;
import java.util.List;

import org.naw.core.Engine;
import org.naw.core.exchange.DefaultMessageExchange;
import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskPipeline;
import org.naw.core.task.support.Tasks;
import org.naw.core.utils.ValueGenerators;
import org.naw.tasks.End;

public class Executable {
	
	private String name;

	private List<Object> tasks;
	
	private TaskPipeline pipeline;

	public String getName() {
		return name;
	}

	public void setExecutableName(String name) {
		this.name = name;
	}

	public void setTasks(List<Object> tasks) {
		this.tasks = tasks;
	}
	
	public TaskPipeline getPipeline() {
		return pipeline;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void initialize(Engine engine) throws Exception {
		if ((tasks == null) || tasks.isEmpty()) {
			throw new IllegalArgumentException("tasks cannot be empty");
		}

		for (int i = 0, n = tasks.size(); i < n; ++i) {
			Object o = tasks.get(i);

			if (!(o instanceof Task)) {
				throw new IllegalArgumentException("task " + o.getClass() + " must derived from " + Task.class + " class");
			}
		}
		
		End task = new End();
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
		return new DefaultMessageExchange(ValueGenerators.messageExchangeId(), name);
	}
	
	@Override
	public String toString() {
		return Executable.class + " [ name: " + name + " ]";
	}
}
