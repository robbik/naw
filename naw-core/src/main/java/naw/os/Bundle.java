package naw.os;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Bundle implements Serializable, Cloneable, Map<String, Object> {

	private static final long serialVersionUID = 3129102777074467061L;
	
	private final Map<String, Object> mValues;
	
	public Bundle() {
		mValues = new HashMap<String, Object>();
	}
	
	public Bundle(Bundle b) {
		mValues = new HashMap<String, Object>(b.mValues);
	}
	
	public int size() {
		return mValues.size();
	}
	
	public boolean isEmpty() {
		return mValues.isEmpty();
	}
	
	public void clear() {
		mValues.clear();
	}
	
	public boolean containsKey(String key) {
		return mValues.containsKey(key);
	}
	
	public Object get(String key) {
		return mValues.get(key);
	}
	
	public void putAll(Bundle b) {
		mValues.putAll(b.mValues);
	}
	
	public Set<String> keySet() {
		return mValues.keySet();
	}
	
	public void putBoolean(String key, boolean value) {
		mValues.put(key, Boolean.valueOf(value));
	}
	
	public void putByte(String key, byte value) {
		mValues.put(key, Byte.valueOf(value));
	}
	
	public void putChar(String key, char value) {
		mValues.put(key, Character.valueOf(value));
	}
	
	public void putShort(String key, short value) {
		mValues.put(key, Short.valueOf(value));
	}
	
	public void putInt(String key, int value) {
		mValues.put(key, Integer.valueOf(value));
	}
	
	public void putLong(String key, long value) {
		mValues.put(key, Long.valueOf(value));
	}
	
	public void putFloat(String key, float value) {
		mValues.put(key, Float.valueOf(value));
	}
	
	public void putDouble(String key, double value) {
		mValues.put(key, Double.valueOf(value));
	}
	
	public void putString(String key, String value) {
		mValues.put(key, value);
	}
	
	public void putCharSequence(String key, CharSequence value) {
		mValues.put(key, value);
	}
	
	public void putSerializable(String key, Serializable value) {
		mValues.put(key, value);
	}
	
	public void putBundle(String key, Bundle value) {
		mValues.put(key, value);
	}
	
	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
        Object o = mValues.get(key);
        if (o == null) {
            return defaultValue;
        }
        
        try {
            return ((Boolean) o).booleanValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
	}
	
	public byte getByte(String key) {
		return getByte(key, (byte) 0);
	}
	
	public byte getByte(String key, byte defaultValue) {
        Object o = mValues.get(key);
        if (o == null) {
            return defaultValue;
        }
        
        try {
            return ((Byte) o).byteValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
	}
	
	public char getChar(String key) {
		return getChar(key, (char) 0);
	}
	
	public char getChar(String key, char defaultValue) {
        Object o = mValues.get(key);
        if (o == null) {
            return defaultValue;
        }
        
        try {
            return ((Character) o).charValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
	}
	
	public short getShort(String key) {
		return getShort(key, (short) 0);
	}
	
	public short getShort(String key, short defaultValue) {
        Object o = mValues.get(key);
        if (o == null) {
            return defaultValue;
        }
        
        try {
            return ((Short) o).shortValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
	}
	
	public int getInt(String key) {
		return getInt(key, 0);
	}
	
	public int getInt(String key, int defaultValue) {
        Object o = mValues.get(key);
        if (o == null) {
            return defaultValue;
        }
        
        try {
            return ((Integer) o).intValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
	}
	
	public long getLong(String key) {
		return getLong(key, 0L);
	}
	
	public long getLong(String key, long defaultValue) {
        Object o = mValues.get(key);
        if (o == null) {
            return defaultValue;
        }
        
        try {
            return ((Long) o).longValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
	}
	
	public float getFloat(String key) {
		return getFloat(key, 0f);
	}
	
	public float getFloat(String key, float defaultValue) {
        Object o = mValues.get(key);
        if (o == null) {
            return defaultValue;
        }
        
        try {
            return ((Float) o).floatValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
	}
	
	public double getDouble(String key) {
		return getDouble(key, 0.0);
	}
	
	public double getDouble(String key, double defaultValue) {
        Object o = mValues.get(key);
        if (o == null) {
            return defaultValue;
        }
        
        try {
            return ((Double) o).doubleValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
	}
	
	public String getString(String key) {
		return getString(key, null);
	}
	
	public String getString(String key, String defaultValue) {
        Object o = mValues.get(key);
        if (o == null) {
            return defaultValue;
        }
        
        try {
            return (String) o;
        } catch (ClassCastException e) {
            return defaultValue;
        }
	}
	
	public CharSequence getCharSequence(String key) {
		return getCharSequence(key, null);
	}
	
	public CharSequence getCharSequence(String key, CharSequence defaultValue) {
        Object o = mValues.get(key);
        if (o == null) {
            return defaultValue;
        }
        
        try {
            return (CharSequence) o;
        } catch (ClassCastException e) {
            return defaultValue;
        }
	}
	
	public Bundle getBundle(String key) {
        Object o = mValues.get(key);
        if (o == null) {
            return null;
        }
        
        try {
            return (Bundle) o;
        } catch (ClassCastException e) {
            return null;
        }
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T getSerializable(String key) {
		if (key == null) {
			return null;
		}
		
        Object o = mValues.get(key);
        if (o == null) {
            return null;
        }
        
        try {
            return (T) o;
        } catch (ClassCastException e) {
            return null;
        }
	}
	
	public boolean remove(String key) {
		return mValues.remove(key) != null;
	}

	@Override
	public Object clone() {
		return new Bundle(this);
	}
	
	@Override
	public String toString() {
		return "Bundle[" + mValues.toString() + "]";
	}

	public boolean containsKey(Object key) {
		if (key == null) {
			return false;
		}

		return mValues.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return mValues.containsValue(value);
	}

	public Set<Map.Entry<String, Object>> entrySet() {
		return mValues.entrySet();
	}

	public Object get(Object key) {
		if (key == null) {
			return null;
		}
		
		return mValues.get(key);
	}

	public Object put(String key, Object value) {
		if (key == null) {
			throw new NullPointerException("key");
		}

		return mValues.put(key, value);
	}

	public void putAll(Map<? extends String, ? extends Object> o) {
		mValues.putAll(o);
	}

	public Object remove(Object key) {
		if (key == null) {
			return null;
		}
		
		return mValues.remove(key);
	}

	public Collection<Object> values() {
		return mValues.values();
	}
}
