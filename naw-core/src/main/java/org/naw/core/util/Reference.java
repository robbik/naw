package org.naw.core.util;

import java.io.Serializable;

public class Reference<T> implements Serializable {

	private static final long serialVersionUID = -1525438891929163843L;

	private T value;

	public Reference() {
		this(null);
	}

	public Reference(T value) {
		this.value = value;
	}

	public void set(T value) {
		this.value = value;
	}

	public T get() {
		return value;
	}

	public void unset() {
		value = null;
	}
}
