package org.naw.core.exchange;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultMessageExchange implements MessageExchange {

	private static final long serialVersionUID = 309868109865219978L;
	
	private static final String ERROR_CODE_KEY = "LAST_ERROR_CODE";
	
	private static final String ERROR_MSG_KEY = "LAST_ERROR_MSG";
	
	protected final String id;
	
	protected final String executableName;
	
	protected final Map<String, Object> vars;

	protected final Map<String, Object> varspriv;
	
	protected volatile int errorCode;
	
	protected volatile String errorMsg;
	
	public DefaultMessageExchange(String id, String executableName) {
		this.id = id;
		this.executableName = executableName;
		
		vars = Collections.synchronizedMap(new HashMap<String, Object>());
		varspriv = Collections.synchronizedMap(new HashMap<String, Object>());
		
		errorCode = 0;
		errorMsg = "No error";
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#getId()
	 */
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * @see org.naw.core.exchange.MessageExchange#getExecutableName()
	 */
	public String getExecutableName() {
		return executableName;
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#set(java.lang.String, java.lang.Object)
	 */
	public void set(String name, Object value) {
		vars.put(name, value);
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#get(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		return (T) vars.get(name);
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#unset(java.lang.String)
	 */
	public void unset(String name) {
		vars.remove(name);
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#setpriv(java.lang.String, java.lang.Object)
	 */
	public void setpriv(String name, Object value) {
		varspriv.put(name, value);
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#getpriv(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getpriv(String name) {
		return (T) varspriv.get(name);
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#unsetpriv(java.lang.String)
	 */
	public void unsetpriv(String name) {
		varspriv.remove(name);
	}

	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#clear()
	 */
	public void clear() {
		vars.clear();
	}

	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		return vars.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		return vars.containsValue(value);
	}

	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#entrySet()
	 */
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return vars.entrySet();
	}

	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#get(java.lang.Object)
	 */
	public Object get(Object name) {
		if (ERROR_CODE_KEY.equals(name)) {
			return Integer.valueOf(errorCode);
		} else if (ERROR_MSG_KEY.equals(name)) {
			return errorMsg;
		} else {
			return vars.get(name);
		}
	}

	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#isEmpty()
	 */
	public boolean isEmpty() {
		return vars.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#keySet()
	 */
	public Set<String> keySet() {
		return vars.keySet();
	}

	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#put(java.lang.String, java.lang.Object)
	 */
	public Object put(String name, Object value) {
		return vars.put(name, value);
	}

	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends String, ? extends Object> map) {
		vars.putAll(map);
	}

	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#remove(java.lang.Object)
	 */
	public Object remove(Object name) {
		return vars.remove(name);
	}

	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#size()
	 */
	public int size() {
		return vars.size();
	}

	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#values()
	 */
	public Collection<Object> values() {
		return vars.values();
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#setLastErrorCode(int)
	 */
	public void setLastErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#getLastErrorCode()
	 */
	public int getLastErrorCode() {
		return errorCode;
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#setLastErrorMessage(java.lang.String)
	 */
	public void setLastErrorMessage(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#getLastErrorMessage()
	 */
	public String getLastErrorMessage() {
		return errorMsg;
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#setLastError(int, java.lang.String)
	 */
	public void setLastError(int code, String msg) {
		this.errorCode = code;
		this.errorMsg = msg;
	}
	
	/* (non-Javadoc)
	 * @see org.naw.core.task.MessageExchange#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " [ errorCode = " + errorCode + " ; public "
				+ vars + "; private " + varspriv + " ]";
	}
}
