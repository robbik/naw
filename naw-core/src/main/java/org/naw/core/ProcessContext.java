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
 * Represent process context (not the process). The implementation of this class
 * must ensure that the class is THREAD-SAFE.
 */
public interface ProcessContext {

	/**
	 * retrieve process context name
	 * 
	 * @return process context name
	 */
	String getName();

	/**
	 * retrieve storage used by this process context
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
	 * get pipeline used to execute this process context
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
	 * initialize this process context (this method MUST be invoked before
	 * creating new process)
	 * 
	 * @throws Exception
	 *             if an error occurred
	 */
	void init() throws Exception;

	/**
	 * find partner link used by this process context using its name
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
	 * creating new process
	 * 
	 * @return new process
	 */
	Process newProcess();

	/**
	 * find process in this context by its id
	 * 
	 * @param pid
	 *            process id
	 * @return process or <code>null</code> if not found
	 */
	Process findProcess(String pid);

	/**
	 * find all processes in this context
	 * 
	 * @return list of processes
	 */
	Collection<Process> findAllProcesses();

	/**
	 * terminate and destroy process
	 * 
	 * @param pid
	 *            process id
	 */
	void terminate(String pid);

	/**
	 * gracefully shutdown this process context
	 */
	void shutdown();

	/**
	 * force shutdown this process context
	 */
	void shutdownNow();

	/**
	 * resume context (wake-up) from hibernate state
	 * 
	 * @throws Exception
	 *             if an error occurred
	 */
	void resume() throws Exception;

	/**
	 * hibernate this process
	 */
	void hibernate();
}
