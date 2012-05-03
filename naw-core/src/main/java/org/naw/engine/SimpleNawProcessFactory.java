package org.naw.engine;

import org.naw.core.task.TaskQueue;

public class SimpleNawProcessFactory implements NawProcessFactory {

	public NawProcess newProcess(String qName, NawEngine engine) {
		return new SimpleNawProcess(qName, engine);
	}

	public NawProcess newProcess(String qName, NawEngine engine,
			TaskQueue taskQueue) {
		return new SimpleNawProcess(qName, engine, taskQueue);
	}
}
