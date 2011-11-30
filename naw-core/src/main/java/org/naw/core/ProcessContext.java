package org.naw.core;

import java.util.Collection;

import org.naw.core.activity.Activity;
import org.naw.core.exchange.Message;
import org.naw.core.partnerLink.PartnerLink;
import org.naw.core.storage.Storage;
import org.naw.core.util.Timer;

/**
 * represent process context or a workflow (not the instance)
 * 
 * @author robbik
 * 
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
	 * creating new workflow instance and specify its message
	 * 
	 * @param message
	 *            the message
	 * @return new workflow instance
	 */
	Process newProcess(Message message);

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
	 * de-activate process, remove from this context, and return the process.
	 * The process is optionally destroyed
	 * 
	 * @param pid
	 *            process id
	 * @param destroyAfter
	 *            if <code>true</code>, the process is destroyed after
	 *            de-activated. Otherwise <code>false</code>
	 * @return the process or <code>null</code> if not found
	 */
	Process deactivate(String pid, boolean destroyAfter);

	/**
	 * terminate and destroy workflow instance
	 * 
	 * @param pid
	 *            workflow instance id
	 */
	void terminate(String pid);

	/**
	 * destroy this workflow
	 */
	void destroy();

	void addLifeCycleListener(ProcessLifeCycleListener listener);

	void removeLifeCycleListener(ProcessLifeCycleListener listener);

	void fireProcessStateChange(Process process, ProcessState newState,
			Activity newActivity);

	void fireProcessBeginWait(Process process, Activity activity);

	void fireProcessEndWait(Process process, Activity activity);
}