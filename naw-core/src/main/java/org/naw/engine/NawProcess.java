package org.naw.engine;

import java.util.Map;

import org.naw.core.task.TaskQueue;

/**
 * represent a process. This class is thread-safe You can do atomic operation by
 * using code: <code>
 *     ...
 *     NawProcess process = ...;
 *     ...
 *     synchronized (process) {
 *         ...
 *     }
 *     ...
 * </code>
 */
public interface NawProcess {

	String getQName();

	/**
	 * get task queue used to execute process activities
	 * 
	 * @return task queue
	 */
	TaskQueue getTaskQueue();

	/**
	 * get workflow engine
	 * 
	 * @return workflow engine
	 */
	NawEngine getEngine();

	/**
	 * set variable value from a map
	 * 
	 * @param map
	 *            the map
	 * @throws NullPointerException
	 *             if map is <code>null</code>
	 */
	void set(Map<String, Object> map);

	/**
	 * set variable value
	 * 
	 * @param name
	 *            variable name
	 * @param value
	 *            value to be set
	 * @throws NullPointerException
	 *             if variable name is <code>null</code>
	 */
	void set(String name, Object value);

	/**
	 * get variable name
	 * 
	 * @param name
	 *            variable name
	 * @return variable value
	 */
	<T> T get(String name);

	/**
	 * unset variable
	 * 
	 * @param name
	 *            variable name
	 * @throws NullPointerException
	 *             if variable name is <code>null</code>
	 */
	void unset(String name);

	Map<String, Object> dump();

	/**
	 * set internal variable value
	 * 
	 * @param name
	 *            variable name
	 * @param value
	 *            value to be set
	 * @throws NullPointerException
	 *             if variable name or value is <code>null</code>
	 */
	void seti(String name, Object value);

	/**
	 * get internal variable name
	 * 
	 * @param name
	 *            variable name
	 * @return variable value, null if variable does not exist
	 */
	<T> T geti(String name);

	/**
	 * unset internal variable name
	 * 
	 * @param name
	 *            variable name
	 */
	void unseti(String name);

	Map<String, Object> dumpi();
}
