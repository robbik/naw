package org.naw.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.naw.core.task.TaskQueue;

public class SimpleNawProcess implements NawProcess {

	private final String qname;

	private final NawEngine engine;

	private final TaskQueue taskQueue;

	private final Map<String, Object> vars;

	private final Map<String, Object> ivars;

	public SimpleNawProcess(String qname, NawEngine engine) {
		this(qname, engine, engine.getTaskQueue());
	}

	public SimpleNawProcess(String qname, NawEngine engine, TaskQueue taskQueue) {
		this.qname = qname;

		this.engine = engine;
		this.taskQueue = taskQueue;

		vars = Collections.synchronizedMap(new HashMap<String, Object>());
		ivars = Collections.synchronizedMap(new HashMap<String, Object>());
	}

	public String getQName() {
		return qname;
	}

	public NawEngine getEngine() {
		return engine;
	}

	public TaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void set(Map<String, Object> map) {
		vars.putAll(map);
	}

	public void set(String name, Object value) {
		vars.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		return (T) vars.get(name);
	}

	public void unset(String name) {
		vars.remove(name);
	}

	public Map<String, Object> dump() {
		return vars;
	}

	public void seti(String name, Object value) {
		ivars.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T geti(String name) {
		return (T) ivars.get(name);
	}

	public void unseti(String name) {
		ivars.remove(name);
	}

	public Map<String, Object> dumpi() {
		return ivars;
	}
}
