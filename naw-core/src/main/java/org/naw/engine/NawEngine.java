package org.naw.engine;

import java.util.Map;

import org.naw.core.task.TaskQueue;
import org.naw.core.task.support.Timer;
import org.naw.engine.config.NawProcessDefinition;
import org.naw.engine.storage.Storage;
import org.naw.tasks.CompletionHandler;

public interface NawEngine {
	
	public static final int STEPPING_NAVAIL = 0;
	
	public static final int STEPPING_WAITING = 1;
	
	public static final int STEPPING_AVAIL = 2;

	/**
	 * get storage used to store/persist processes
	 * 
	 * @return storage
	 */
	Storage getStorage();

	/**
	 * get task queue used to execute process activities
	 * 
	 * @return task queue
	 */
	TaskQueue getTaskQueue();

	/**
	 * get shared timer
	 * 
	 * @return timer
	 */
	Timer getTimer();

	/**
	 * set process factory
	 * 
	 * @param factory
	 *            the process factory
	 */
	void setProcessFactory(NawProcessFactory factory);

	/**
	 * execute process definition and return the running process
	 * 
	 * @param def
	 *            process definition
	 * @return the running process
	 */
	NawProcess exec(NawProcessDefinition def) throws Exception;

	/**
	 * execute process definition with given initial data and return the running
	 * process
	 * 
	 * @param def
	 *            process definition
	 * @param data
	 *            process initial data
	 * @return the running process
	 */
	NawProcess exec(NawProcessDefinition def, Map<String, Object> data)
			throws Exception;

	/**
	 * execute process definition and return the running process
	 * 
	 * @param def
	 *            process definition
	 * @param handler
	 *            completion handler for the process
	 * @return the running process
	 */
	NawProcess exec(NawProcessDefinition def, CompletionHandler handler)
			throws Exception;

	/**
	 * execute process definition with given initial data and return the running
	 * process
	 * 
	 * @param def
	 *            process definition
	 * @param data
	 *            process initial data
	 * @param handler
	 *            completion handler for the process
	 * @return the running process
	 */
	NawProcess exec(NawProcessDefinition def, Map<String, Object> data,
			CompletionHandler handler) throws Exception;

	int stepping();
}
