package org.naw.core.activity;

import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.pipeline.Pipeline;

/**
 * activity context, unique per {@link Activity}
 */
public interface ActivityContext {

	/**
	 * get process context pipeline, shortcut for
	 * <code>getProcessContext().getPipeline()</code>
	 * 
	 * @return
	 */
	Pipeline getPipeline();

	/**
	 * get process context
	 * 
	 * @return process context
	 */
	ProcessContext getProcessContext();

	/**
	 * get activity that has this context
	 * 
	 * @return activity
	 */
	Activity getActivity();

	/**
	 * execute next activity in the pipeline
	 * 
	 * @param process
	 */
	void execute(Process process);

	/**
	 * This method is executed when process context is hibernating
	 */
	void hibernate();
}
