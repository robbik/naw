package org.naw.core.task;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataExchange implements Serializable, Map<String, Object> {

	private static final long serialVersionUID = 309868109865219978L;
	
	private static final String ERROR_CODE_KEY = "LAST_ERROR_CODE";
	
	private static final String ERROR_MSG_KEY = "LAST_ERROR_MSG";
	
	private final Map<String, Object> vars;

	private final Map<String, Object> varspriv;
	
	private volatile int errorCode;
	
	private volatile String errorMsg;
	
	public DataExchange() {
		vars = Collections.synchronizedMap(new HashMap<String, Object>());
		varspriv = Collections.synchronizedMap(new HashMap<String, Object>());
		
		errorCode = 0;
		errorMsg = "No error";
	}
	
	public void set(String name, Object value) {
		vars.put(name, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		return (T) vars.get(name);
	}
	
	public void unset(String name) {
		vars.remove(name);
	}
	
	public void setpriv(String name, Object value) {
		varspriv.put(name, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getpriv(String name) {
		return (T) varspriv.get(name);
	}
	
	public void unsetpriv(String name) {
		varspriv.remove(name);
	}

	public void clear() {
		vars.clear();
	}

	public boolean containsKey(Object key) {
		return vars.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return vars.containsValue(value);
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return vars.entrySet();
	}

	public Object get(Object name) {
		if (ERROR_CODE_KEY.equals(name)) {
			return Integer.valueOf(errorCode);
		} else if (ERROR_MSG_KEY.equals(name)) {
			return errorMsg;
		} else {
			return vars.get(name);
		}
	}

	public boolean isEmpty() {
		return vars.isEmpty();
	}

	public Set<String> keySet() {
		return vars.keySet();
	}

	public Object put(String name, Object value) {
		return vars.put(name, value);
	}

	public void putAll(Map<? extends String, ? extends Object> map) {
		vars.putAll(map);
	}

	public Object remove(Object name) {
		return vars.remove(name);
	}

	public int size() {
		return vars.size();
	}

	public Collection<Object> values() {
		return vars.values();
	}
	
	public void setLastErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public int getLastErrorCode() {
		return errorCode;
	}
	
	public void setLastErrorMessage(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public String getLastErrorMessage() {
		return errorMsg;
	}
	
	public void setLastError(int code, String msg) {
		this.errorCode = code;
		this.errorMsg = msg;
	}
	
	@Override
	public String toString() {
		return super.toString() + " [ errorCode = " + errorCode + " ; public "
				+ vars + "; private " + varspriv + " ]";
	}
}
