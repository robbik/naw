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
import org.osgi.framework.Version;

import rk.commons.util.ObjectHelper;

public class Executable {
	
	private String qualifiedName;
	
	private String name;
	
	private Version version;

	private List<Object> tasks;
	
	private TaskPipeline pipeline;
	
	private volatile boolean suspended; 

	public String getQualifiedName() {
		return qualifiedName;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setVersion(Version version) {
		this.version = version;
	}
	
	public Version getVersion() {
		return version;
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
		
		qualifiedName = name + ":" + version.toString();
		
		End task = new End();
		task.setObjectQName(qualifiedName + "#end");
		
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
	
	public void suspend() {
		suspended = true;
	}
	
	public void resume() {
		suspended = false;
	}
	
	public boolean isSuspended() {
		return suspended;
	}
	
	public MessageExchange createMessageExchange() {
		return new DefaultMessageExchange(ValueGenerators.messageExchangeId(), qualifiedName);
	}
	
	@Override
	public String toString() {
		return Executable.class + " (" + name + "; version=" + version + ")";
	}
	
	@Override
	public int hashCode() {
		return name.hashCode() ^ version.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		
		if (!(object instanceof Executable)) {
			return false;
		}
		
		Executable other = (Executable) object;
		
		return ObjectHelper.equals(name, other.name) && ObjectHelper.equals(version, other.version);
	}
}
