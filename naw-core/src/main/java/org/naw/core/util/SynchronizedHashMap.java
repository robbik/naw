package org.naw.core.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SynchronizedHashMap<K, V> extends HashMap<K, V> {

	private static final long serialVersionUID = 1561817686391007967L;

	public SynchronizedHashMap() {
		super();
	}

	public SynchronizedHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public SynchronizedHashMap(int initialCapacity) {
		super(initialCapacity);
	}

	public SynchronizedHashMap(Map<? extends K, ? extends V> m) {
		super(m);
	}

	@Override
	public synchronized void clear() {
		super.clear();
	}

	@Override
	public synchronized Object clone() {
		return super.clone();
	}

	@Override
	public synchronized boolean containsKey(Object key) {
		return super.containsKey(key);
	}

	@Override
	public synchronized boolean containsValue(Object value) {
		return super.containsValue(value);
	}

	@Override
	public synchronized Set<java.util.Map.Entry<K, V>> entrySet() {
		return super.entrySet();
	}

	@Override
	public synchronized V get(Object key) {
		return super.get(key);
	}

	@Override
	public synchronized boolean isEmpty() {
		return super.isEmpty();
	}

	@Override
	public synchronized Set<K> keySet() {
		return super.keySet();
	}

	@Override
	public synchronized V put(K key, V value) {
		return super.put(key, value);
	}

	@Override
	public synchronized void putAll(Map<? extends K, ? extends V> map) {
		super.putAll(map);
	}

	@Override
	public synchronized V remove(Object key) {
		return super.remove(key);
	}

	@Override
	public synchronized int size() {
		return super.size();
	}

	@Override
	public synchronized Collection<V> values() {
		return super.values();
	}

	@Override
	public synchronized boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	public synchronized int hashCode() {
		return super.hashCode();
	}

	@Override
	public synchronized String toString() {
		return super.toString();
	}
}
