package org.naw.core.activity;

import org.naw.core.Process;

/**
 * Represent process activity, unique per process context and shared
 * across processes in the same process context
 */
public interface Activity {

	/**
	 * get activity name
	 * 
	 * @return activity name
	 */
	String getName();

	/**
	 * get context of this activity
	 * 
	 * @return activity context
	 */
	ActivityContext getActivityContext();

	/**
	 * initialize activity
	 * 
	 * @param ctx
	 *            activity context
	 * @throws Exception
	 *             if an error occurred
	 */
	void init(ActivityContext ctx) throws Exception;

	/**
	 * execute this activity
	 * 
	 * @param process
	 *            the process who needs to execute this activity
	 * @throws Exception
	 *             if an error occurred
	 */
	void execute(Process process) throws Exception;

	/**
	 * force wake-up this sleeping activity
	 */
	void wakeUp(Process process) throws Exception;

	/**
	 * hibernate this activity
	 */
	void hibernate();

	/**
	 * shutdown this activity
	 */
	void shutdown();
}
