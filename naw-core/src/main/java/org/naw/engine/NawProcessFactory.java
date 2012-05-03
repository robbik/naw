package org.naw.engine;

import org.naw.core.task.TaskQueue;

public interface NawProcessFactory {

	NawProcess newProcess(String qName, NawEngine engine);

	NawProcess newProcess(String qName, NawEngine engine, TaskQueue taskQueue);
}
