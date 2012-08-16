package naw.os;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import naw.content.BroadcastReceiver;
import naw.content.Context;
import naw.content.Intent;
import naw.content.IntentFilter;

public class BroadcastManager {
	
	private static final int MSG_EXEC_PENDING_BROADCASTS = 0x7810274;
	
    private static final Object sLock = new Object();
    
    private static Map<Context, BroadcastManager> sInstances;
    
    static BroadcastManager getInstance(Context context) {
    	BroadcastManager instance = null;
    	
    	context = context.getApplicationContext();
    	
        synchronized (sLock) {
            if (sInstances == null) {
            	sInstances = new HashMap<Context, BroadcastManager>();
            } else {
            	instance = sInstances.get(context);
            }
            
            if (instance == null) {
            	instance = new BroadcastManager(context.getApplicationContext());
            	sInstances.put(context, instance);
            }
        }
        
        return instance;
    }
    
    private final Context mContext;
    
    private final Handler mHandler;
    
    private final Map<BroadcastReceiver, List<IntentFilter>> mReceivers;
    
    private final Map<String, List<ReceiverRecord>> mActions;
    
    private final List<BroadcastRecord> mPendingBroadcasts;

    private BroadcastManager(Context context) {
    	mContext = context;
    	
    	mHandler = new Handler(context.getMainLooper()) {
    		
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_EXEC_PENDING_BROADCASTS:
					executePendingBroadcasts();
					break;
				default:
					super.handleMessage(msg);
					break;
				}
			}
    	};
    	
    	mReceivers = new HashMap<BroadcastReceiver, List<IntentFilter>>();
    	mActions = new HashMap<String, List<ReceiverRecord>>();
    	
    	mPendingBroadcasts = new ArrayList<BroadcastRecord>();
	}
	
	/*package*/ void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        synchronized (mReceivers) {
            ReceiverRecord entry = new ReceiverRecord(filter, receiver);
            
            List<IntentFilter> filters = mReceivers.get(receiver);
            if (filters == null) {
                filters = new ArrayList<IntentFilter>(1);
                
                mReceivers.put(receiver, filters);
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
	
	/*package*/ void unregisterReceiver(BroadcastReceiver receiver) {
        synchronized (mReceivers) {
            List<IntentFilter> filters = mReceivers.remove(receiver);
            if (filters == null) {
                return;
            }
            
            for (int i = 0, n_i = filters.size(); i < n_i; ++i) {
                IntentFilter filter = filters.get(i);
                
                for (String action : filter.actions()) {
                    List<ReceiverRecord> receivers = mActions.get(action);
                    if (receivers != null) {
                        for (int k = 0, n_k = receivers.size(); k < n_k; ++k) {
                            if (receivers.get(k).mReceiver == receiver) {
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
	
	/*package*/ boolean sendBroadcast(Intent intent) {
        synchronized (mReceivers) {
            List<ReceiverRecord> entries = mActions.get(intent.getAction());
            
            if (entries != null) {
                ArrayList<ReceiverRecord> receivers = null;
                
                for (int i = 0, n = entries.size(); i < n; ++i) {
                    ReceiverRecord receiver = entries.get(i);

                    if (receiver.mBroadcasting) {
                        continue;
                    }

                    int match = receiver.mFilter.match(intent);
                    if (match >= 0) {
                        if (receivers == null) {
                            receivers = new ArrayList<ReceiverRecord>();
                        }
                        
                        receivers.add(receiver);
                        receiver.mBroadcasting = true;
                    }
                }

                if (receivers != null) {
                    for (int i = 0, n = receivers.size(); i < n; ++i) {
                        receivers.get(i).mBroadcasting = false;
                    }
                    
                    mPendingBroadcasts.add(new BroadcastRecord(intent, receivers));
                    
                    if (!mHandler.hasMessages(MSG_EXEC_PENDING_BROADCASTS)) {
	                	mHandler.sendEmptyMessage(MSG_EXEC_PENDING_BROADCASTS);
                    }
                    
                    return true;
                }
            }
        }
        
        return false;
	}
	
	/*package*/ void executePendingBroadcasts() {
		BroadcastRecord[] brs;
		
		int n;
		
		while (true) {
	        synchronized (mReceivers) {
	            n = mPendingBroadcasts.size();
	            if (n <= 0) {
	                return;
	            }
	            
	            brs = new BroadcastRecord[n];
	            
	            mPendingBroadcasts.toArray(brs);
	            mPendingBroadcasts.clear();
	        }
	        
	        for (int i = 0; i < n; ++i) {
	        	BroadcastRecord br = brs[i];
	        	
		        Intent intent = br.mIntent;
				List<ReceiverRecord> receivers = br.mReceivers;
				
				for (int j = 0, n_j = receivers.size(); j < n_j; ++j) {
					receivers.get(j).mReceiver.onReceive(mContext, intent);
				}
	        }
		}
	}

	/*package*/ static class ReceiverRecord {
		/*package*/ final IntentFilter mFilter;
        
        /*package*/ final BroadcastReceiver mReceiver;
        
        /*package*/ boolean mBroadcasting;

        /*package*/ ReceiverRecord(IntentFilter filter, BroadcastReceiver receiver) {
            mFilter = filter;
            mReceiver = receiver;
            mBroadcasting = false;
        }
    }

    /*package*/ static class BroadcastRecord {
    	/*package*/ final Intent mIntent;
        
    	/*package*/ final ArrayList<ReceiverRecord> mReceivers;

    	/*package*/ BroadcastRecord(Intent intent, ArrayList<ReceiverRecord> receivers) {
            mIntent = intent;
            mReceivers = receivers;
        }
    }
}
