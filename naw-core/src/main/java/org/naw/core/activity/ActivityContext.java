package org.naw.core.activity;

import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.pipeline.Pipeline;

/**
 * activity context
 */
public interface ActivityContext {

	Pipeline getPipeline();

	ProcessContext getProcessContext();

	Activity getActivity();

	/**
	 * execute next activity in the pipeline
	 * 
	 * @param process
	 */
	void execute(Process process);
}
