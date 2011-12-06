package org.naw.core;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.naw.core.activity.Activity;
import org.naw.core.exchange.Message;
import org.naw.core.util.Timeout;

/**
 * represent a process. This class is thread-safe and serializable. You can do
 * atomic operation by using code: <code>
 *     ...
 *     Process process = ...;
 *     ...
 *     synchronized (process) {
 *         ...
 *     }
 *     ...
 * </code>
 */
public interface Process extends Serializable {

	/**
	 * retrieve this process id
	 * 
	 * @return the id
	 */
	String getId();

	/**
	 * retrieve this process context
	 * 
	 * @return the process context
	 */
	ProcessContext getContext();

	/**
	 * retrieve process context name
	 * 
	 * @return process context name
	 */
	String getContextName();

	/**
	 * set process context name. This method is intended for storage use only.
	 * 
	 * @param contextName
	 */
	void setContextName(String contextName);

	/**
	 * set attribute
	 * 
	 * @param name
	 *            attribute name
	 * @param value
	 *            attribute value
	 */
	void setAttribute(String name, Object value);

	/**
	 * retrieve attribute
	 * 
	 * @param name
	 *            attribute name
	 * @param type
	 *            expected attribute value type
	 * @return attribute value or <code>null</code> if not found
	 */
	<T> T getAttribute(String name, Class<T> type);

	/**
	 * remove attribute and return its old value
	 * 
	 * @param name
	 *            attribute name
	 * @param type
	 *            expected old value value type
	 * @return old value value or <code>null</code> if not found
	 */
	<T> T removeAttribute(String name, Class<T> type);

	/**
	 * retrieve attributes as map
	 * 
	 * @return attributes
	 */
	Map<String, Object> getAttributes();

	/**
	 * get message used by this process
	 * 
	 * @return the message
	 */
	Message getMessage();

	/**
	 * register timeout to timeout pool
	 * 
	 * @param timeout
	 *            timeout
	 */
	void registerTimeout(Timeout timeout);

	/**
	 * remove timeout from the pool
	 * 
	 * @param timeout
	 *            timeout to be removed
	 */
	void unregisterTimeout(Timeout timeout);

	/**
	 * cancel specific activity timeout registered in the pool
	 * 
	 * @param activityName
	 *            activity name
	 */
	void cancelTimeout(String activityName);

	/**
	 * retrieve all timeouts registered in the pool as list
	 * 
	 * @return all timeouts registered in the pool
	 */
	List<Timeout> getTimeouts();

	/**
	 * compare current state and activity with expected state and activity
	 * atomically.
	 * 
	 * @param state
	 *            expected state
	 * @param activity
	 *            expected activity
	 * @return <code>true</code> if current state and current activity equals to
	 *         expected state and expected activity.
	 */
	boolean compare(ProcessState state, Activity activity);

	/**
	 * update current state and activity atomically without firing event to
	 * registered life cycle listeners
	 * 
	 * @param newState
	 *            new state
	 * @param newActivity
	 *            new activity
	 */
	void noFireEventUpdate(ProcessState newState, Activity newActivity);

	/**
	 * update current state and activity atomically and fire event to registered
	 * life cycle listeners
	 * 
	 * @param newState
	 *            new state
	 * @param newActivity
	 *            new activity
	 */
	void update(ProcessState newState, Activity newActivity);

	/**
	 * update current state and fire event to registered life cycle listeners
	 * 
	 * @param newState
	 *            new state
	 */
	void update(ProcessState newState);

	/**
	 * compare current state and activity with expected state and activity. If
	 * equals update current state with new state and current activity with new
	 * activity. This method do the same as: <code>
	 *    ...
	 *    if (compare(state, activity)) {
	 *        update(state, activity);
	 *    }
	 *    ...
	 * </code>
	 * 
	 * except this method do it atomically.
	 * 
	 * @param state
	 *            expected state
	 * @param activity
	 *            expected activity
	 * @param newState
	 *            new state
	 * @param newActivity
	 *            new activity
	 * @return <code>true</code> if current state and current activity equals to
	 *         expected state and expected activity.
	 */
	boolean compareAndUpdate(ProcessState state, Activity activity,
			ProcessState newState, Activity newActivity);

	/**
	 * compare current state and activity with expected state and activity. If
	 * equals update current state with new state. The activity is not updated.
	 * This method do the same as: <code>
	 *    ...
	 *    compareAndUpdate(state, activity, newState, activity)
	 *    ...
	 * </code>
	 * 
	 * except this method do it atomically.
	 * 
	 * @param state
	 *            expected state
	 * @param activity
	 *            expected activity
	 * @param newState
	 *            new state
	 * @return <code>true</code> if current state and current activity equals to
	 *         expected state and expected activity.
	 */
	boolean compareAndUpdate(ProcessState state, Activity activity,
			ProcessState newState);

	/**
	 * get current state
	 * 
	 * @return current state
	 */
	ProcessState getState();

	/**
	 * get current activity
	 * 
	 * @return current activity
	 */
	Activity getActivity();

	/**
	 * create and auto-resume this process. this method is USED INTERNALLY.
	 * 
	 * @param ctx
	 *            process context
	 */
	void init(ProcessContext ctx);

	/**
	 * destroy this process, this method is USED INTERNALLY.
	 */
	void destroy();
}
