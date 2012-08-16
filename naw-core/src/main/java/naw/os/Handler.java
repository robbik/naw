package naw.os;

public class Handler {
	
	private MessageQueue mQueue;
	
    public Handler() {
        Looper mLooper = Looper.myLooper();
        if (mLooper == null) {
            throw new RuntimeException(
                "Can't create handler inside thread that has not called Looper.prepare()");
        }
        
        mQueue = mLooper.mQueue;
    }

    public Handler(Looper looper) {
        mQueue = looper.mQueue;
    }

    public final Message obtainMessage() {
        return Message.obtain(this);
    }

    public final Message obtainMessage(int what, Object o) {
        return Message.obtain(this, what, o);
    }
    
    public final boolean sendMessage(Message msg) {
        return sendMessageDelayed(msg, 0);
    }

    public final boolean sendMessage(int what, Object obj) {
    	Message msg = Message.obtain();
    	msg.what = what;
    	msg.obj = obj;
    	
        return sendMessageDelayed(msg, 0);
    }

    public final boolean sendEmptyMessage(int what) {
        return sendEmptyMessageDelayed(what, 0);
    }

    public final boolean sendEmptyMessageDelayed(int what, Object obj, long delayMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        
        return sendMessageDelayed(msg, delayMillis);
    }

    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        
        return sendMessageDelayed(msg, delayMillis);
    }

    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        
        return sendMessageAtTime(msg, uptimeMillis);
    }

    public final boolean sendMessageDelayed(Message msg, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        
        return sendMessageAtTime(msg, System.currentTimeMillis() + delayMillis);
    }

    public boolean sendMessageAtTime(int what, Object obj, long deadline) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        
        return sendMessageAtTime(msg, deadline);
    }
    
    public boolean sendMessageAtTime(Message msg, long deadline) {
        boolean sent = false;
        
        final MessageQueue queue = mQueue;
        if (queue != null) {
            msg.target = this;
            sent = queue.enqueueMessage(msg, deadline);
        }
        
        return sent;
    }
    
    public boolean hasMessages(int what) {
    	return mQueue != null && mQueue.hasMessage(what);
    }

    public void handleMessage(Message msg) {
    	// do nothing
    }
}
