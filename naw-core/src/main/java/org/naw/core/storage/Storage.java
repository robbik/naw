package org.naw.core.storage;

import org.naw.core.Process;

/**
 * represent dehydration storage
 */
public interface Storage {

	/**
	 * persist workflow instance
	 * 
	 * @param process
	 *            workflow instance
	 * @return <code>true</code> if persisted, <code>false</code> otherwise
	 */
	boolean persist(Process process);

	/**
	 * remove workflow instance from this storage because the process is already
	 * terminated
	 * 
	 * @param pid
	 *            workflow instance if
	 */
	void remove(String pid);

	/**
	 * find workflow instance by its id
	 * 
	 * @param pid
	 *            workflow instance id
	 * @return workflow instance or <code>null</code> if not found
	 */
	Process find(String pid);

	/**
	 * find all workflow instance
	 * 
	 * @return array of workflow instance, cannot be <code>null</code>
	 */
	Process[] findAll();
}
