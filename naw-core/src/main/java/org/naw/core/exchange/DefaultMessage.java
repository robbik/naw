package org.naw.core.exchange;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link Message}
 */
public class DefaultMessage implements Message {

	private static final long serialVersionUID = 5747231707387987057L;

	private final Map<String, Map<String, Object>> var;

	public DefaultMessage() {
		var = new HashMap<String, Map<String, Object>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.exchange.Message#declare(java.lang.String)
	 */
	public synchronized void declare(String variable) {
		if (!var.containsKey(variable)) {
			var.put(variable, new HashMap<String, Object>());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.exchange.Message#remove(java.lang.String)
	 */
	public synchronized Map<String, Object> remove(String variable) {
		Map<String, Object> value = var.remove(variable);
		if (value == null) {
			return null;
		}

		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.exchange.Message#getVariables()
	 */
	public synchronized Set<String> getVariables() {
		return var.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.exchange.Message#set(java.lang.String, java.util.Map)
	 */
	public void set(String variable, Map<String, Object> value) {
		if (value != null) {
			synchronized (this) {
				var.put(variable, value);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.exchange.Message#get(java.lang.String)
	 */
	public synchronized Map<String, Object> get(String variable) {
		Map<String, Object> value = var.get(variable);
		if (value == null) {
			return null;
		}

		return value;
	}

	public synchronized void clear() {
		var.clear();
	}

	@Override
	public synchronized Object clone() throws CloneNotSupportedException {
		DefaultMessage c = new DefaultMessage();
		c.var.putAll(var);
		
		return c;
	}

	@Override
	public synchronized String toString() {
		return super.toString() + " [vars=" + var + "]";
	}
}
