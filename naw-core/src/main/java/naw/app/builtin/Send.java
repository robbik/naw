package naw.app.builtin;

import java.util.concurrent.atomic.AtomicBoolean;

import naw.app.Activity;
import naw.content.BroadcastReceiver;
import naw.content.Context;
import naw.content.Intent;
import naw.content.IntentFilter;
import naw.os.Bundle;

public class Send extends Activity {

	private String mTo;
	
	private boolean mOneWay;

	private String mRequestVariable;
	
	private String mResponseVariable;
	
	private String mResponseIntentAction;
	
	private String mResponseEnabled;
	
	public Send(Context base, String id) {
		super(base, id);
		
		mResponseIntentAction = id + "____Response";
		mResponseEnabled = id + "____ResponseEnabled";
	}

	public void setTo(String to) {
		mTo = to;
	}
	
	public void setOneWay(boolean oneWay) {
		mOneWay = oneWay;
	}

	public void setRequestVariable(String variable) {
		mRequestVariable = variable;
	}

	public void setResponseVariable(String variable) {
		mResponseVariable = variable;
	}
	
	@Override
	protected void onCreate() {
		if (!mOneWay) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(mResponseIntentAction);
			
			BroadcastReceiver receiver = new BroadcastReceiver() {
				
				public void onReceive(Context context, Intent intent) {
					Send.this.onResponse(intent);
				}
			};
			
			registerReceiver(receiver, filter);
		}
	}
	
	@Override
	protected void onStart(final Intent intent) {
		final String processId = intent.getProcessId();
		final Bundle pex = getProcessExtras(processId);
		
		if (!mOneWay) {
			pex.put(mResponseEnabled, new AtomicBoolean(true));
		}
		
		Intent broadcastIntent = new Intent(processId);
		broadcastIntent.setAction(mTo);
		broadcastIntent.putExtras(pex.getBundle(mRequestVariable));
		
		sendBroadcast(broadcastIntent);
		
		if (mOneWay) {
			finish(processId);
		}
	}
	
	/*package*/ void onResponse(Intent intent) {
		final String processId = intent.getProcessId();
		final Bundle pex = getProcessExtras(processId);
		
		AtomicBoolean enabled = (AtomicBoolean) pex.get(mResponseEnabled);
		if (enabled.compareAndSet(true, false)) {
			pex.putBundle(mResponseVariable, intent.getExtras());
			
			finish(processId);
		}
	}
}
