package naw.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import naw.content.Context;
import naw.content.Intent;
import naw.content.IntentFilter;

public class ActivityManager {
	
    private static final Object sLock = new Object();
    
    private static Map<Context, ActivityManager> sInstances;
    
    public static ActivityManager getInstance(Context context) {
    	ActivityManager instance = null;
    	
    	context = context.getApplicationContext();
    	
        synchronized (sLock) {
            if (sInstances == null) {
            	sInstances = new HashMap<Context, ActivityManager>();
            } else {
            	instance = sInstances.get(context);
            }
            
            if (instance == null) {
            	instance = new ActivityManager(context.getApplicationContext());
            	sInstances.put(context, instance);
            }
        }
        
        return instance;
    }
    
    private final Context mContext;
    
    private final Map<Activity, List<IntentFilter>> mReceivers;
    
    private final Map<String, List<ReceiverRecord>> mActions;
    
    private final Map<String, Activity> mActivities;
    
    private final Map<Activity, Set<Activity>> mOnFinish;

    private ActivityManager(Context context) {
    	mContext = context;
    	
    	mReceivers = new HashMap<Activity, List<IntentFilter>>();
    	mActions = new HashMap<String, List<ReceiverRecord>>();
    	
    	mActivities = new HashMap<String, Activity>();
    	
    	mOnFinish = new HashMap<Activity, Set<Activity>>();
	}
	
	public void register(Activity activity) {
        synchronized (mReceivers) {
        	mActivities.put(activity.getActivityId(), activity);
        }
	}
	
	public void register(Activity activity, IntentFilter filter) {
        synchronized (mReceivers) {
        	mActivities.put(activity.getActivityId(), activity);
        	
            ReceiverRecord entry = new ReceiverRecord(filter, activity);
            
            List<IntentFilter> filters = mReceivers.get(activity);
            if (filters == null) {
                filters = new ArrayList<IntentFilter>(1);
                
                mReceivers.put(activity, filters);
            }
            
            filters.add(filter);
            
            for (String action : filter.actions()) {
                List<ReceiverRecord> entries = mActions.get(action);
                if (entries == null) {
                    entries = new ArrayList<ReceiverRecord>(1);
                    
                    mActions.put(action, entries);
                }
                
                entries.add(entry);
            }
        }
	}
	
	public void unregister(Activity activity) {
        synchronized (mReceivers) {
        	mActivities.remove(activity.getActivityId());
        	
            List<IntentFilter> filters = mReceivers.remove(activity);
            if (filters == null) {
                return;
            }
            
            for (int i = 0, n_i = filters.size(); i < n_i; ++i) {
                IntentFilter filter = filters.get(i);
                
                for (String action : filter.actions()) {
                    List<ReceiverRecord> receivers = mActions.get(action);
                    
                    if (receivers != null) {
                        for (int k = 0, n_k = receivers.size(); k < n_k; ++k) {
                            if (receivers.get(k).mReceiver == activity) {
                                receivers.remove(k);
                                --k;
                            }
                        }
                        
                        if (receivers.isEmpty()) {
                            mActions.remove(action);
                        }
                    }
                }
            }
        }
    }
	
	public boolean broadcastIntent(Intent intent) {
		final String activityId = intent.getActivityId();
		
		Activity target = null;
        
        synchronized (mReceivers) {    
        	if (activityId != null) {
        		target = mActivities.get(activityId);
        	} else {
	            List<ReceiverRecord> entries = mActions.get(intent.getAction());
	            
	            if (entries != null) {
	                for (int i = 0, n = entries.size(); i < n; ++i) {
	                    ReceiverRecord rr = entries.get(i);
	                    
	                    int match = rr.mFilter.match(intent);
	                    if (match >= 0) {
	                    	target = rr.mReceiver;
	                        break;
	                    }
	                }
	            }
        	}
        }
        
        if (target != null) {
        	target.performStart(intent);
        	
        	return true;
        } else {
        	return false;
        }
	}
	
	public ActivitySelector select(Activity activity) {
		return new ActivitySelector(activity);
	}
	
	public void finishActivity(String processId, Activity activity) {
		Set<Activity> acts;
		
		synchronized (mOnFinish) {
			acts = mOnFinish.get(activity);
		}
		
		if (acts != null) {
			synchronized (acts) {
				for (Activity a : acts) {
					Intent intent = new Intent(processId);
					intent.setActivity(a);
					
					mContext.startActivity(intent);
				}
			}
		}
	}
	
	public void finishActivity(String processId, Activity activity, long deadline) {
		Set<Activity> acts;
		
		synchronized (mOnFinish) {
			acts = mOnFinish.get(activity);
		}
		
		if (acts != null) {
			synchronized (acts) {
				for (Activity a : acts) {
					Intent intent = new Intent(processId);
					intent.setActivity(a);
					
					mContext.startActivity(intent, deadline);
				}
			}
		}
	}

	/*package*/ static class ReceiverRecord {
		/*package*/ final IntentFilter mFilter;
        
        /*package*/ final Activity mReceiver;
        
        /*package*/ ReceiverRecord(IntentFilter filter, Activity receiver) {
            mFilter = filter;
            mReceiver = receiver;
        }
    }

	/*package*/ public class ActivitySelector {
        /*package*/ final Activity mActivity;
        
        /*package*/ ActivitySelector(Activity activity) {
        	mActivity = activity;
        }
        
        public void onFinish(Activity activity) {
        	ActivityManager am = ActivityManager.this;
        	
        	Set<Activity> acts;
        	
        	synchronized (am.mOnFinish) {
        		acts = am.mOnFinish.get(mActivity);
        		
        		if (acts == null) {
        			acts = new HashSet<Activity>();
        			am.mOnFinish.put(mActivity, acts);
        		}
        	}
        	
        	synchronized (acts) {
        		acts.add(activity);
        	}
        }
    }
}
