package org.naw.engine.storage;

import org.naw.engine.NawProcess;

/**
 * represent storage
 */
public interface Storage {

	/**
	 * persist process
	 * 
	 * @param process
	 *            the process
	 * @return <code>true</code> if persisted, <code>false</code> otherwise
	 */
	boolean persist(NawProcess process);
}
