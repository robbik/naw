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
}
