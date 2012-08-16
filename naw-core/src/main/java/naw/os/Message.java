package naw.os;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {

	private static final long serialVersionUID = 2341130293754146484L;

	/*package*/ static final int FLAG_IN_USE = 1;
	
	/*package*/ static final int FLAGS_RESERVED = ~FLAG_IN_USE;
	
	/*package*/ static final int FLAGS_TO_CLEAR_ON_COPY_FROM = FLAGS_RESERVED | FLAG_IN_USE;
	
	private static final int MAX_POOL_SIZE = 100;
	
	private static final Object sPoolSync = new Object();
	
	private static Message sPool;
	
	private static int sPoolSize = 0;
	
	public static final String MAP_KEY_FLAGS = "flags";
	
	public static final String MAP_KEY_WHAT = "what";
	
	public static final String MAP_KEY_OBJECT = "object";
	
	public static final String MAP_KEY_WHEN = "when";

	public static final String MAP_KEY_TARGET = "target";

	public static final String MAP_KEY_DATA = "data";

	public static final String MAP_KEY_NEXT = "next";

	/*package*/ int flags;
	
	/*package*/ int what;
	
	/*package*/ Object obj;
	
	/*package*/ long when;
	
	/*package*/ Handler target;
	
	/*package*/ Bundle data;
	
	/*package*/ Message next;
	
    public static Message obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                Message m = sPool;
                sPool = m.next;
                
                m.next = null;
                --sPoolSize;
                
                return m;
            }
        }
        
        return new Message();
    }

    public static Message obtain(Handler h) {
    	Message m = obtain();
    	m.target = h;
    	
    	return m;
    }

    public static Message obtain(Handler h, int what, Object obj) {
    	Message m = obtain();
    	m.what = what;
    	m.target = h;
    	m.obj = obj;
    	
    	return m;
    }

	public void recycle() {
        clearForRecycle();

        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }
	
	public int getWhat() {
		return what;
	}
	
	public boolean is(int what) {
		return this.what == what;
	}

    public long getWhen() {
        return when;
    }
    
    public Object getObject() {
    	return obj;
    }

    public Bundle getData() {
        if (data == null) {
            data = new Bundle();
        }
        
        return data;
    }

    public Bundle peekData() {
        return data;
    }

    public void setData(Bundle data) {
        this.data = data;
    }
    
    public Message getNext() {
    	return next;
    }

    /*package*/ void clearForRecycle() {
        flags = 0;
        what = 0;
        when = 0;
        target = null;
        obj = null;
        data = null;
        next = null;
    }

    /*package*/ boolean isInUse() {
        return ((flags & FLAG_IN_USE) == FLAG_IN_USE);
    }

    /*package*/ void markInUse() {
        flags |= FLAG_IN_USE;
    }

    private Message() {
    	// do nothing
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
    	out.writeInt(flags);
    	out.writeInt(what);
    	out.writeLong(when);
    	
    	if (target instanceof Serializable) {
    		out.writeObject(target);
    	} else {
    		out.writeObject(null);
    	}
    	
    	if (obj instanceof Serializable) {
    		out.writeObject(obj);
    	} else {
    		out.writeObject(null);
    	}
    	
		out.writeObject(data);
		
		out.writeObject(next);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	flags = in.readInt();
    	what = in.readInt();
    	when = in.readLong();
    	
    	target = (Handler) in.readObject();
    	
    	obj = in.readObject();
    	
    	data = (Bundle) in.readObject();
    	
    	next = (Message) in.readObject();
    }
    
    public Map<String, Object> asMap() {
    	Map<String, Object> map = new HashMap<String, Object>();
    	
    	map.put(MAP_KEY_FLAGS, Integer.valueOf(flags));
    	map.put(MAP_KEY_WHAT, Integer.valueOf(what));
    	map.put(MAP_KEY_WHEN, Long.valueOf(when));
    	
    	if (target != null) {
    		map.put(MAP_KEY_TARGET, target);
    	}
    	
    	if (obj != null) {
    		map.put(MAP_KEY_OBJECT, obj);
    	}

    	if (data != null) {
    		map.put(MAP_KEY_DATA, data);
    	}

    	if (next != null) {
    		map.put(MAP_KEY_NEXT, next);
    	}

    	return map;
    }
}
