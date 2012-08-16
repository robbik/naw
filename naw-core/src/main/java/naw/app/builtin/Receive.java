package naw.app.builtin;

import java.util.concurrent.atomic.AtomicBoolean;

import naw.app.Activity;
import naw.content.BroadcastReceiver;
import naw.content.Context;
import naw.content.Intent;
import naw.content.IntentFilter;
import naw.os.Bundle;

public class Receive extends Activity {
	
	private String mFrom;
	
	private String mVariable;
	
	private boolean mCreateInstance;
	
	private String mReceiveEnabled;
	
	public Receive(Context base, String id) {
		super(base, id);
		
		mReceiveEnabled = id + "____ReceiveEnabled";
	}
	
	@Override
	protected void onCreate() {
		if (!mCreateInstance) {
			BroadcastReceiver receiver = new BroadcastReceiver() {
				
				public void onReceive(Context context, Intent intent) {
					Receive.this.onReceive(intent);
				}
			};
			
			IntentFilter filter = new IntentFilter();
			filter.addAction(mFrom);
			
			registerReceiver(receiver, filter);
		}
	}
	
	@Override
	protected void onStart(Intent intent) {
		if (mCreateInstance) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(mFrom);
			
			BroadcastReceiver mReceiver = new BroadcastReceiver() {
				
				public void onReceive(Context context, Intent intent) {
					Receive.this.onReceive(intent);
				}
			};
			
			registerReceiver(mReceiver, filter);
		} else {
			final Bundle pex = getProcessExtras(intent.getProcessId());
			
			pex.put(mReceiveEnabled, new AtomicBoolean(true));
		}
	}
	
	/*package*/ void onReceive(Intent intent) {
		final String processId;
		final Bundle pex;
		
		if (mCreateInstance) {
			processId = createProcess();
			pex = getProcessExtras(processId);
		} else {
			processId = intent.getProcessId();
			pex = getProcessExtras(processId);
			
			AtomicBoolean enabled = (AtomicBoolean) pex.get(mReceiveEnabled);
			if (!enabled.compareAndSet(true, false)) {
				return;
			}
		}
		
		pex.putBundle(mVariable, intent.getExtras());
		
		finish(processId);
	}
}
