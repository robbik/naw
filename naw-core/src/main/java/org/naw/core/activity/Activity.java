package org.naw.core.activity;

import org.naw.core.Process;

public interface Activity {

	/**
	 * get activity name
	 * 
	 * @return activity name
	 */
	String getName();

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
	 * destroy this activity
	 */
	void destroy();
}
