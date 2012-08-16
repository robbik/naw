package naw.app;

import naw.content.Context;
import naw.content.ContextWrapper;
import naw.content.Intent;

public abstract class Activity extends ContextWrapper {

	private ActivityManager mActivityManager;
	
	private String mId;
	
	protected Activity(Context base, String id) {
		super(base);
		
		mActivityManager = ActivityManager.getInstance(base);
		mId = id;
	}
	
	public final String getActivityId() {
		return mId;
	}
	
	public final void finish(String processId) {
		mActivityManager.finishActivity(processId, this);
	}
	
	public final void finish(String processId, long deadline) {
		mActivityManager.finishActivity(processId, this, deadline);
	}
	
	final void performCreate() {
		onCreate();
	}
	
	final void performStart(Intent intent) {
		onStart(intent);
	}
	
	final void performResume(Intent intent) {
		onResume(intent);
	}
	
	final void performDestroy() {
		onDestroy();
	}
	
	protected void onCreate() {
		//
	}
	
	protected void onStart(Intent intent) {
		//
	}
	
	protected void onResume(Intent intent) {
		//
	}
	
	protected void onDestroy() {
		//
	}
	
	@Override
	public int hashCode() {
		return mId.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o == null) {
			return false;
		} else if (o instanceof Activity) {
			return mId.equals(((Activity) o).mId);
		} else {
			return false;
		}
	}
}
