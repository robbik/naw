package org.naw.core.exchange;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link Message}
 */
public class DefaultMessage implements Message {

	private static final long serialVersionUID = 5747231707387987057L;

	private ConcurrentHashMap<String, Map<String, Object>> var;

	public DefaultMessage() {
		var = new ConcurrentHashMap<String, Map<String, Object>>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.naw.exchange.Message#declare(java.lang.String)
	 */
	public void declare(String variable) {
		var.putIfAbsent(variable, new HashMap<String, Object>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.exchange.Message#remove(java.lang.String)
	 */
	public Map<String, Object> remove(String variable) {
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
	public Set<String> getVariables() {
		return var.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.exchange.Message#set(java.lang.String, java.util.Map)
	 */
	public void set(String variable, Map<String, Object> value) {
		if (value != null) {
			var.put(variable, value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.naw.exchange.Message#get(java.lang.String)
	 */
	public Map<String, Object> get(String variable) {
		Map<String, Object> value = var.get(variable);
		if (value == null) {
			return null;
		}

		return value;
	}

	@Override
	public String toString() {
		return super.toString() + " [vars=" + var + "]";
	}
}
