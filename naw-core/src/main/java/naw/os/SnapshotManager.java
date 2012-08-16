package naw.os;

import java.util.HashMap;
import java.util.Map;

import naw.content.Context;

public abstract class SnapshotManager {
	
    private static final Object sLock = new Object();
    
    private static Map<Context, SnapshotManager> sInstances;
    
    public static SnapshotManager getInstance(Context context) {
    	SnapshotManager instance = null;
    	
        synchronized (sLock) {
            if (sInstances != null) {
            	instance = sInstances.get(context.getApplicationContext());
            }
        }
        
        return instance;
    }
    
    public static void setInstance(Context context, SnapshotManager instance) {
    	context = context.getApplicationContext();
    	
        synchronized (sLock) {
            if (sInstances == null) {
            	sInstances = new HashMap<Context, SnapshotManager>();
            } else {
            	instance = sInstances.get(context);
            }
            
            if (instance == null) {
            	sInstances.put(context, instance);
            }
        }
    }
    
    public abstract void begin();
    
    public abstract void snapshot(Message msg);
    
    public abstract void snapshot(String pid, Bundle extras);
    
    public abstract void end(boolean commit);
}
