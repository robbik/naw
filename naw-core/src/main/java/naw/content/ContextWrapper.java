package naw.content;

import naw.os.Bundle;
import naw.os.Looper;

public abstract class ContextWrapper implements Context {

	private final Context mBase;

	protected ContextWrapper(Context base) {
		mBase = base;
	}
	
	public Context getBaseContext() {
		return mBase;
	}
	
	public Context getApplicationContext() {
		return mBase.getApplicationContext();
	}
	
	public Looper getMainLooper() {
		return mBase.getMainLooper();
	}
	
	public void snapshot() {
		mBase.snapshot();
	}

	public void startActivity(Intent intent) {
		mBase.startActivity(intent);
	}

	public void startActivity(Intent intent, long deadline) {
		mBase.startActivity(intent, deadline);
	}

	public void sendBroadcast(Intent intent) {
		mBase.sendBroadcast(intent);
	}

	public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
		mBase.registerReceiver(receiver, filter);
	}
	
	public Bundle getProcessExtras(String processId) {
		return mBase.getProcessExtras(processId);
	}
	
	public String createProcess() {
		return mBase.createProcess();
	}
}
