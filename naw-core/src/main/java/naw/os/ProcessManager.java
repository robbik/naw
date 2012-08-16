package naw.os;

import java.util.HashMap;
import java.util.Map;

import naw.content.Context;

public class ProcessManager {
	
    private static final Object sLock = new Object();
    
    private static Map<Context, ProcessManager> sInstances;
    
    static ProcessManager getInstance(Context context) {
    	ProcessManager instance = null;
    	
        synchronized (sLock) {
            if (sInstances == null) {
            	sInstances = new HashMap<Context, ProcessManager>();
            } else {
            	instance = sInstances.get(context);
            }
            
            if (instance == null) {
            	instance = new ProcessManager(context.getApplicationContext());
            	sInstances.put(context, instance);
            }
        }
        
        return instance;
    }
    
    private Map<String, Bundle> mExtras;

    private ProcessManager(Context context) {
    	mExtras = new HashMap<String, Bundle>();
    }
    
    public Bundle getProcessExtras(String pid) {
    	synchronized (mExtras) {
    		Bundle extras = mExtras.get(pid);
    		
    		if (extras == null) {
    			extras = new SynchronizedBundle();
    			mExtras.put(pid, extras);
    		}
    		
    		return extras;
    	}
    }
    
    public void snapshot(SnapshotManager sm) {
    	synchronized (mExtras) {
    		for (Map.Entry<String, Bundle> e : mExtras.entrySet()) {
    			sm.snapshot(e.getKey(), e.getValue());
    		}
    	}
    }
}
