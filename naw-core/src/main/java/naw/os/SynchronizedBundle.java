package naw.os;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SynchronizedBundle extends Bundle {

	private static final long serialVersionUID = -1313386017440426519L;
	
	private boolean mModified;
	
	public SynchronizedBundle() {
		super();
	}

	public SynchronizedBundle(Bundle b) {
		super(b);
	}
	
	public synchronized boolean hasModified() {
		return mModified;
	}
	
	public synchronized void modified() {
		mModified = true;
	}
	
	/*package*/ synchronized void unmodified() {
		mModified = false;
	}

	@Override
	public synchronized int size() {
		return super.size();
	}

	@Override
	public synchronized boolean isEmpty() {
		return super.isEmpty();
	}

	@Override
	public synchronized void clear() {
		super.clear();
	}

	@Override
	public synchronized boolean containsKey(String key) {
		return super.containsKey(key);
	}

	@Override
	public synchronized Object get(String key) {
		return super.get(key);
	}

	@Override
	public synchronized void putAll(Bundle b) {
		super.putAll(b);
		mModified = true;
	}

	@Override
	public synchronized Set<String> keySet() {
		return super.keySet();
	}

	@Override
	public synchronized void putBoolean(String key, boolean value) {
		super.putBoolean(key, value);
		mModified = true;
	}

	@Override
	public synchronized void putByte(String key, byte value) {
		super.putByte(key, value);
		mModified = true;
	}

	@Override
	public synchronized void putChar(String key, char value) {
		super.putChar(key, value);
		mModified = true;
	}

	@Override
	public synchronized void putShort(String key, short value) {
		super.putShort(key, value);
		mModified = true;
	}

	@Override
	public synchronized void putInt(String key, int value) {
		super.putInt(key, value);
		mModified = true;
	}

	@Override
	public synchronized void putLong(String key, long value) {
		super.putLong(key, value);
		mModified = true;
	}

	@Override
	public synchronized void putFloat(String key, float value) {
		super.putFloat(key, value);
		mModified = true;
	}

	@Override
	public synchronized void putDouble(String key, double value) {
		super.putDouble(key, value);
		mModified = true;
	}

	@Override
	public synchronized void putString(String key, String value) {
		super.putString(key, value);
		mModified = true;
	}

	@Override
	public synchronized void putCharSequence(String key, CharSequence value) {
		super.putCharSequence(key, value);
		mModified = true;
	}

	@Override
	public synchronized void putSerializable(String key, Serializable value) {
		super.putSerializable(key, value);
		mModified = true;
	}

	@Override
	public synchronized void putBundle(String key, Bundle value) {
		super.putBundle(key, value);
		mModified = true;
	}

	@Override
	public synchronized boolean getBoolean(String key) {
		return super.getBoolean(key);
	}

	@Override
	public synchronized boolean getBoolean(String key, boolean defaultValue) {
		return super.getBoolean(key, defaultValue);
	}

	@Override
	public synchronized byte getByte(String key) {
		return super.getByte(key);
	}

	@Override
	public synchronized byte getByte(String key, byte defaultValue) {
		return super.getByte(key, defaultValue);
	}

	@Override
	public synchronized char getChar(String key) {
		return super.getChar(key);
	}

	@Override
	public synchronized char getChar(String key, char defaultValue) {
		return super.getChar(key, defaultValue);
	}

	@Override
	public synchronized short getShort(String key) {
		return super.getShort(key);
	}

	@Override
	public synchronized short getShort(String key, short defaultValue) {
		return super.getShort(key, defaultValue);
	}

	@Override
	public synchronized int getInt(String key) {
		return super.getInt(key);
	}

	@Override
	public synchronized int getInt(String key, int defaultValue) {
		return super.getInt(key, defaultValue);
	}

	@Override
	public synchronized long getLong(String key) {
		return super.getLong(key);
	}

	@Override
	public synchronized long getLong(String key, long defaultValue) {
		return super.getLong(key, defaultValue);
	}

	@Override
	public synchronized float getFloat(String key) {
		return super.getFloat(key);
	}

	@Override
	public synchronized float getFloat(String key, float defaultValue) {
		return super.getFloat(key, defaultValue);
	}

	@Override
	public synchronized double getDouble(String key) {
		return super.getDouble(key);
	}

	@Override
	public synchronized double getDouble(String key, double defaultValue) {
		return super.getDouble(key, defaultValue);
	}

	@Override
	public synchronized String getString(String key) {
		return super.getString(key);
	}

	@Override
	public synchronized String getString(String key, String defaultValue) {
		return super.getString(key, defaultValue);
	}

	@Override
	public synchronized CharSequence getCharSequence(String key) {
		return super.getCharSequence(key);
	}

	@Override
	public synchronized CharSequence getCharSequence(String key, CharSequence defaultValue) {
		return super.getCharSequence(key, defaultValue);
	}

	@Override
	public synchronized Bundle getBundle(String key) {
		return super.getBundle(key);
	}

	@Override
	public synchronized <T extends Serializable> T getSerializable(String key) {
		return super.getSerializable(key);
	}

	@Override
	public synchronized boolean remove(String key) {
		if (super.remove(key)) {
			mModified = true;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized Object clone() {
		return super.clone();
	}

	@Override
	public synchronized String toString() {
		return super.toString();
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
	public synchronized Set<Map.Entry<String, Object>> entrySet() {
		return super.entrySet();
	}

	@Override
	public synchronized Object get(Object key) {
		return super.get(key);
	}

	@Override
	public synchronized Object put(String key, Object value) {
		Object o = super.put(key, value);
		mModified = true;
		
		return o;
	}

	@Override
	public synchronized void putAll(Map<? extends String, ? extends Object> o) {
		super.putAll(o);
		mModified = true;
	}

	@Override
	public synchronized Object remove(Object key) {
		Object o = super.remove(key);
		if (o != null) {
			mModified = true;
		}
		
		return o;
	}

	@Override
	public synchronized Collection<Object> values() {
		return super.values();
	}
}
