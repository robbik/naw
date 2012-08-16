package naw.content;

import naw.os.Bundle;
import naw.os.Looper;

public interface Context {
	
	Context getApplicationContext();
	
	Looper getMainLooper();
	
	void snapshot();
	
	void startActivity(Intent intent);
	
	void startActivity(Intent intent, long deadline);
	
	void sendBroadcast(Intent intent);
	
	void registerReceiver(BroadcastReceiver receiver, IntentFilter filter);
	
	Bundle getProcessExtras(String processId);
	
	String createProcess();
}
