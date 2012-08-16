package naw.os;

import naw.app.ActivityManager;
import naw.content.BroadcastReceiver;
import naw.content.Context;
import naw.content.Intent;
import naw.content.IntentFilter;
import naw.util.ValueGenerator;

public class ApplicationContext implements Context {
	
	private static final int MSG_START_ACTIVITY = 0x1523121;
	
	private final Looper mMainLooper;
	
	private final MessageQueue mQueue;
	
	private final Handler mHandler;
	
	private final BroadcastManager mBroadcastManager;
	
	private final ActivityManager mActivityManager;
	
	private final ProcessManager mProcessManager;
	
	public ApplicationContext() {
		mMainLooper = new Looper();
		
		mQueue = mMainLooper.mQueue;
		
		mHandler = new Handler(mMainLooper) {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_START_ACTIVITY:
					onStartActivity((Intent) msg.obj);
					break;
				default:
					super.handleMessage(msg);
					break;
				}
			}
		};
		
		mBroadcastManager = BroadcastManager.getInstance(this);
		
		mActivityManager = ActivityManager.getInstance(this);
		
		mProcessManager = ProcessManager.getInstance(this);
	}
	
	public Context getApplicationContext() {
		return this;
	}
	
	public Looper getMainLooper() {
		return mMainLooper;
	}
	
	public void snapshot() {
		SnapshotManager sm = SnapshotManager.getInstance(this);
		if (sm != null) {
			sm.begin();
			
			boolean commit = false;
			
			try {
				synchronized (mQueue) {
					sm.snapshot(mQueue.mMessages);
				}
				
				commit = true;
			} finally {
				sm.end(commit);
			}
		}
	}
	
	public void startActivity(Intent intent) {
		mHandler.sendMessage(MSG_START_ACTIVITY, intent);
	}
	
	public void startActivity(Intent intent, long deadline) {
		mHandler.sendMessageAtTime(MSG_START_ACTIVITY, intent, deadline);
	}
	
	public void sendBroadcast(Intent intent) {
		mBroadcastManager.sendBroadcast(intent);
	}
	
	public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
		mBroadcastManager.registerReceiver(receiver, filter);
	}
	
	public Bundle getProcessExtras(String processId) {
		return mProcessManager.getProcessExtras(processId);
	}
	
	public String createProcess() {
		return ValueGenerator.processId();
	}
	
	/*package*/ void onStartActivity(Intent intent) {
		mActivityManager.broadcastIntent(intent);
	}
}
