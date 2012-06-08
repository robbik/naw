package org.naw.core.exchange;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface MessageExchange extends Serializable, Map<String, Object> {

	String getId();
	
	String getExecutableName();

	void set(String name, Object value);

	<T> T get(String name);

	void unset(String name);

	void setpriv(String name, Object value);

	<T> T getpriv(String name);

	void unsetpriv(String name);

	void clear();

	boolean containsKey(Object key);

	boolean containsValue(Object value);

	Set<Map.Entry<String, Object>> entrySet();

	Object get(Object name);

	boolean isEmpty();

	Set<String> keySet();

	Object put(String name, Object value);

	void putAll(Map<? extends String, ? extends Object> map);

	Object remove(Object name);

	int size();

	Collection<Object> values();

	void setLastErrorCode(int errorCode);

	int getLastErrorCode();

	void setLastErrorMessage(String errorMsg);

	String getLastErrorMessage();

	void setLastError(int code, String msg);

	String toString();
}
