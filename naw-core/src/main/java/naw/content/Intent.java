package naw.content;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import naw.app.Activity;
import naw.os.Bundle;

public class Intent implements Serializable, Cloneable {

	private static final long serialVersionUID = -5532850625878967293L;
	
	public static final String ACTION_VIEW = "naw.intent.action.VIEW";

	public static final String EXTRA_CORRELATION = "naw.intent.extra.CORRELATION";
	
	public static final String EXTRA_ATTACHMENT = "naw.intent.extra.ATTACHMENT";
	
	private final String mProcessId;

	private String mAction;
	
	private String mActivityId;

    private HashSet<String> mCategories;

    private Bundle mExtras;
    
    public Intent() {
    	mProcessId = null;
    }

	public Intent(String processId) {
		mProcessId = processId;
	}

	public Intent(String processId, String action) {
		mProcessId = processId;
		
		setAction(action);
	}
	
	public Intent(Intent o) {
		mProcessId = o.mProcessId;
		mAction = o.mAction;
		
        if (o.mCategories != null) {
            mCategories = new HashSet<String>(o.mCategories);
        }
        
        if (o.mExtras != null) {
        	mExtras = new Bundle(o.mExtras);
        }
	}

    @Override
    public Object clone() {
        return new Intent(this);
    }
    
    public String getProcessId() {
    	return mProcessId;
    }

	public Intent setAction(String action) {
		mAction = action == null ? null : action.intern();
		return this;
	}
	
	public String getAction() {
		return mAction;
	}
	
	public void setActivityId(String activityId) {
		mActivityId = activityId;
	}
	
	public void setActivity(Activity activity) {
		mActivityId = activity.getActivityId();
	}
	
	public String getActivityId() {
		return mActivityId;
	}

	public boolean hasCategory(String category) {
        return mCategories != null && mCategories.contains(category);
    }

    public Set<String> getCategories() {
        return mCategories;
    }

    public Intent addCategory(String category) {
        if (mCategories == null) {
            mCategories = new HashSet<String>();
        }
        
        mCategories.add(category.intern());
        return this;
    }

    public void removeCategory(String category) {
        if (mCategories != null) {
            mCategories.remove(category);
            
            if (mCategories.isEmpty()) {
                mCategories = null;
            }
        }
    }

	public boolean hasExtra(String name) {
		return mExtras != null && mExtras.containsKey(name);
	}

	public boolean getBooleanExtra(String name, boolean defaultValue) {
		return mExtras == null ? defaultValue : mExtras.getBoolean(name,
				defaultValue);
	}

	public byte getByteExtra(String name, byte defaultValue) {
		return mExtras == null ? defaultValue : mExtras.getByte(name,
				defaultValue);
	}

	public short getShortExtra(String name, short defaultValue) {
		return mExtras == null ? defaultValue : mExtras.getShort(name,
				defaultValue);
	}

	public char getCharExtra(String name, char defaultValue) {
		return mExtras == null ? defaultValue : mExtras.getChar(name,
				defaultValue);
	}

	public int getIntExtra(String name, int defaultValue) {
		return mExtras == null ? defaultValue : mExtras.getInt(name,
				defaultValue);
	}

	public long getLongExtra(String name, long defaultValue) {
		return mExtras == null ? defaultValue : mExtras.getLong(name,
				defaultValue);
	}

	public float getFloatExtra(String name, float defaultValue) {
		return mExtras == null ? defaultValue : mExtras.getFloat(name,
				defaultValue);
	}

	public double getDoubleExtra(String name, double defaultValue) {
		return mExtras == null ? defaultValue : mExtras.getDouble(name,
				defaultValue);
	}

	public String getStringExtra(String name) {
		return mExtras == null ? null : mExtras.getString(name);
	}

	public CharSequence getCharSequenceExtra(String name) {
		return mExtras == null ? null : mExtras.getCharSequence(name);
	}

	public <T extends Serializable> T getSerializableExtra(String name) {
		return mExtras == null ? null : mExtras.<T> getSerializable(name);
	}

	public Bundle getBundleExtra(String name) {
		return mExtras == null ? null : mExtras.getBundle(name);
	}

	public Bundle getExtras() {
		return mExtras;
	}
	
    public Intent putExtra(String name, boolean value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        mExtras.putBoolean(name, value);
        return this;
    }

    public Intent putExtra(String name, byte value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        
        mExtras.putByte(name, value);
        return this;
    }

    public Intent putExtra(String name, char value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        
        mExtras.putChar(name, value);
        return this;
    }

    public Intent putExtra(String name, short value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        
        mExtras.putShort(name, value);
        return this;
    }

    public Intent putExtra(String name, int value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        
        mExtras.putInt(name, value);
        return this;
    }

    public Intent putExtra(String name, long value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        
        mExtras.putLong(name, value);
        return this;
    }

    public Intent putExtra(String name, float value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        
        mExtras.putFloat(name, value);
        return this;
    }

    public Intent putExtra(String name, double value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        
        mExtras.putDouble(name, value);
        return this;
    }

    public Intent putExtra(String name, String value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        
        mExtras.putString(name, value);
        return this;
    }

    public Intent putExtra(String name, CharSequence value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        
        mExtras.putCharSequence(name, value);
        return this;
    }

    public Intent putExtra(String name, Serializable value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        
        mExtras.putSerializable(name, value);
        return this;
    }
	
    public Intent putExtra(String name, Bundle value) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        
        mExtras.putBundle(name, value);
        return this;
    }

    public Intent putExtras(Intent src) {
        if (src.mExtras != null) {
            if (mExtras == null) {
                mExtras = new Bundle(src.mExtras);
            } else {
                mExtras.putAll(src.mExtras);
            }
        }
        
        return this;
    }

    public Intent putExtras(Bundle extras) {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        
        mExtras.putAll(extras);
        return this;
    }

    public Intent replaceExtras(Intent src) {
        mExtras = src.mExtras != null ? new Bundle(src.mExtras) : null;
        return this;
    }

    public Intent replaceExtras(Bundle extras) {
        mExtras = extras != null ? new Bundle(extras) : null;
        return this;
    }

    public void removeExtra(String name) {
        if (mExtras != null) {
            mExtras.remove(name);
            
            if (mExtras.size() == 0) {
                mExtras = null;
            }
        }
    }
}
