package org.naw.core.storage;

import org.naw.core.Process;

/**
 * represent dehydration storage
 */
public interface Storage {

	/**
	 * persist process
	 * 
	 * @param process
	 *            the process
	 * @return <code>true</code> if persisted, <code>false</code> otherwise
	 */
	boolean persist(Process process);

	/**
	 * remove process from this storage because the process is already
	 * terminated
	 * 
	 * @param pid
	 *            the process
	 */
	void remove(Process process);

	/**
	 * find process by its id
	 * 
	 * @param pid
	 *            process id
	 * @return process or <code>null</code> if not found
	 */
	Process find(String pid);

	/**
	 * find all processes that has context name <code>contextName</code>
	 * 
	 * @param contextName
	 *            the context name
	 * 
	 * @return array of process, cannot be <code>null</code>
	 */
	Process[] findByProcessContext(String contextName);

	/**
	 * find all processes
	 * 
	 * @return array of process, cannot be <code>null</code>
	 */
	Process[] findAll();
}
