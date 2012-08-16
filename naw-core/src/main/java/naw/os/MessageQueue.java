package naw.os;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageQueue {

	/*package*/ Message mMessages;
	
	private final Semaphore mSemaphore;
	
	private final AtomicInteger mRefs;
	
	/*package*/ MessageQueue() {
		mSemaphore = new Semaphore(0, true);
		
		mRefs = new AtomicInteger(0);
	}

	/*package*/ final Message next() throws InterruptedException {
        int nextTimeout = 0;
        
        for (;;) {
        	if (nextTimeout > 0) {
        		mSemaphore.tryAcquire(1, nextTimeout, TimeUnit.MILLISECONDS);
        	} else {
        		mSemaphore.acquire(1);
        	}

            synchronized (this) {
                final long now = System.currentTimeMillis();
                final Message msg = mMessages;
                
                if (msg != null) {
                	if (msg.target == null) {
                		return null;
                	}
                	
                    final long when = msg.when;
                    
                    if (now >= when) {
                        mMessages = msg.next;
                        msg.next = null;
                        
                        msg.markInUse();
                        return msg;
                    } else {
                        nextTimeout = (int) Math.min(when - now, Integer.MAX_VALUE);
                    }
                } else {
                    nextTimeout = -1;
                }
            }
        }
    }

    /*package*/ final boolean enqueueMessage(Message msg, long when) {
        if (msg.isInUse()) {
            throw new IllegalStateException(msg + " This message is already in use.");
        }
        
        synchronized (this) {
            msg.when = when;
            
            Message p = mMessages;
            
            if ((p == null) || (when == 0) || (when < p.when)) {
                msg.next = p;
                mMessages = msg;
            } else {
                Message prev = null;
                
                while ((p != null) && (p.when <= when)) {
                    prev = p;
                    p = p.next;
                }
                
                msg.next = prev.next;
                prev.next = msg;
            }
        }
        
        mSemaphore.release(1);
        return true;
    }
    
    /*package*/ final boolean hasMessage(int what) {
    	synchronized (this) {
    		Message p = mMessages;
    		
            while ((p != null) && (p.what != what)) {
                p = p.next;
            }
            
            return p != null;
    	}
    }
    
    /*package*/ final void incRef() {
    	mRefs.incrementAndGet();
    }
    
    /*package*/ final void decRef() {
    	mRefs.decrementAndGet();
    }
    
    /*package*/ final int refs() {
    	return mRefs.get();
    }
}
