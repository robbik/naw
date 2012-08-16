package naw.os;

import naw.content.Context;

public class Looper {
	
	private static final ThreadLocal<Looper> sInstance = new ThreadLocal<Looper>();
	
	/*package*/ final MessageQueue mQueue;
    
	/*package*/ Looper() {
        mQueue = new MessageQueue();
    }
    
	private Looper(MessageQueue queue) {
        mQueue = queue;
    }
    
    public static void prepare(Context context) {
        if (sInstance.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        
        sInstance.set(new Looper(context.getMainLooper().mQueue));
    }
    
    public static void loop() {
        Looper me = myLooper();
        if (me == null) {
            throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
        }
        
        final MessageQueue queue = me.mQueue;
        
        while (true) {
            Message msg;
            
			try {
				msg = queue.next(); // might block
			} catch (InterruptedException e) {
				break;
			}
            
            if (msg != null) {
            	if (msg.target == null) {
            		msg.recycle();
            		return; // quit looper
            	}
            	
            	msg.target.handleMessage(msg);
            	
                msg.recycle();
            }
        }
    }
    
    public static boolean next() {
        Looper me = myLooper();
        if (me == null) {
            throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
        }
        
        final MessageQueue queue = me.mQueue;
        
        Message msg;
        
		try {
			msg = queue.next(); // might block
		} catch (InterruptedException e) {
			return false;
		}
        
        if (msg == null) {
        	return false;
        } else if (msg.target == null) {
        	return false; // quit looper
        } else {
        	msg.target.handleMessage(msg);
            
            msg.recycle();
            return true;
        }
    }
    
    public static Looper myLooper() {
        return sInstance.get();
    }
    
    public void quit() {
    	int refs = mQueue.refs();
    	
    	for (int i = 0; i < refs; ++i) {
	    	Message msg = Message.obtain();
	    	mQueue.enqueueMessage(msg, 0);
    	}
    }
}
