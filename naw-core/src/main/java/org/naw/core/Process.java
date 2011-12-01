package org.naw.core;

import java.io.Serializable;

import org.naw.core.activity.Activity;
import org.naw.core.exchange.Message;
import org.naw.core.util.Timeout;

/**
 * represent workflow instance
 * 
 * @author robbik
 * 
 */
public interface Process extends Serializable {

	/**
	 * retrieve id of this workflow instance
	 * 
	 * @return the id
	 */
	String getId();

	/**
	 * retrieve this workflow instance workflow
	 * 
	 * @return the workflow
	 */
	ProcessContext getContext();

	void setAttribute(String name, Object value);

	<T> T getAttribute(String name, Class<T> type);

	<T> T removeAttribute(String name, Class<T> type);

	/**
	 * get message used by this workflow instance
	 * 
	 * @return the message
	 */
	Message getMessage();

	void registerTimeout(Timeout timeout);

	void unregisterTimeout(Timeout timeout);

	void cancelTimeout(String activityName);

	void update(ProcessState newState, Activity newActivity);

	void update(ProcessState newState);

	boolean compareAndUpdate(ProcessState state, Activity activity,
			ProcessState newState, Activity newActivity);

	boolean compareAndUpdate(ProcessState state, Activity activity,
			ProcessState newState);

	ProcessState getState();

	Activity getActivity();

	void init(ProcessContext ctx);

	/**
	 * destroy this workflow instance, this method is USED INTERNALLY.
	 */
	void destroy();
}
