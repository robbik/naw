package org.naw.core;

/**
 * Factory design-pattern to create a new process
 */
public interface ProcessFactory {

	/**
	 * create new un-initialized process with default parameters
	 * 
	 * @return new process
	 */
	Process newProcess();

	/**
	 * create new un-initialized process with default parameters and given pid
	 * 
	 * @param pid
	 *            process id
	 * @return new process
	 */
	Process newProcess(String pid);
}
