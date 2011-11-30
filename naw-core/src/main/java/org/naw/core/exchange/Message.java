package org.naw.core.exchange;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * represent a message used by a workflow instance
 */
public interface Message extends Serializable {

	/**
	 * set variable as empty map
	 * 
	 * @param variable
	 */
	void declare(String variable);

	/**
	 * remove variable and return its old value
	 * 
	 * @param variable
	 *            variable name
	 * @return old value or <code>null</code> if not found
	 */
	Map<String, Object> remove(String variable);

	/**
	 * retrieve variables
	 * 
	 * @return set of variable names
	 */
	Set<String> getVariables();

	/**
	 * set variable value
	 * 
	 * @param variable
	 *            variable name
	 * @param value
	 *            the value
	 */
	void set(String variable, Map<String, Object> value);

	/**
	 * retrieve variable value
	 * 
	 * @param variable
	 *            variable name
	 * @return variable value, <code>null</code> if not exists
	 */
	Map<String, Object> get(String variable);
}
