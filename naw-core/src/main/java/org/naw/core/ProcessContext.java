package org.naw.core;

import java.util.Collection;
import java.util.concurrent.Executor;

import org.naw.core.activity.Activity;
import org.naw.core.listener.LifeCycleListener;
import org.naw.core.partnerLink.PartnerLink;
import org.naw.core.pipeline.Pipeline;
import org.naw.core.storage.Storage;
import org.naw.core.util.Selector;
import org.naw.core.util.Timer;

/**
 * Represent process context or a workflow (not the instance). The
 * implementation of this class must ensure that the class is THREAD-SAFE.
 */
public interface ProcessContext {

	/**
	 * retrieve workflow name
	 * 
	 * @return workflow name
	 */
	String getName();

	/**
	 * retrieve storage used by this workflow
	 * 
	 * @return the storage
	 */
	Storage getStorage();

	/**
	 * get timer used to manage timed based activity
	 * 
	 * @return the timer
	 */
	Timer getTimer();

	/**
	 * get pipeline used to execute this workflow
	 * 
	 * @return pipeline
	 */
	Pipeline getPipeline();

	/**
	 * get shared executor
	 * 
	 * @return shared executor
	 */
	Executor getExecutor();

	/**
	 * get selector used to maintain life cycle listener registration
	 * 
	 * @return selector
	 */
	Selector<LifeCycleListener> getSelector();

	/**
	 * initialize this workflow (this method MUST be invoked before creating new
	 * process / workflow instance)
	 * 
	 * @throws Exception
	 *             if an error occurred
	 */
	void init() throws Exception;

	/**
	 * find partner link used by this workflow using its name
	 * 
	 * @param name
	 *            partner link name
	 * @return the partner link or <code>null</code> if not found
	 */
	PartnerLink findPartnerLink(String name);

	/**
	 * find activity by its name
	 * 
	 * @param name
	 *            activity name
	 * @return activity, <code>null</code> if not found
	 */
	Activity findActivity(String name);

	/**
	 * register activity
	 * 
	 * @param activity
	 *            the activity
	 * @throws Exception
	 *             if the activity name already registered but with different
	 *             object
	 */
	void registerActivity(Activity activity) throws Exception;

	/**
	 * creating new workflow instance
	 * 
	 * @return new workflow instance
	 */
	Process newProcess();

	/**
	 * find workflow instance in this context by its id
	 * 
	 * @param pid
	 *            workflow instance id
	 * @return workflow instance or <code>null</code> if not found
	 */
	Process findProcess(String pid);

	/**
	 * find all workflow instances in this context
	 * 
	 * @return list of workflow instance
	 */
	Collection<Process> findAllProcesses();

	/**
	 * add process into this context and activate it
	 * 
	 * @param process
	 *            the process
	 * @throws Exception
	 *             if an error occurred or the same process has been found
	 */
	void activate(Process process) throws Exception;

	/**
	 * terminate and destroy workflow instance
	 * 
	 * @param pid
	 *            workflow instance id
	 */
	void terminate(String pid);

	/**
	 * gracefully shutdown this workflow
	 */
	void shutdown();

	/**
	 * force shutdown this workflow
	 */
	void shutdownNow();

	void hibernate();
}
