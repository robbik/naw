package naw.app.builtin;

import java.util.List;

import naw.app.Activity;
import naw.app.ActivityManager;
import naw.content.Context;
import naw.content.Intent;
import naw.content.Predicate;

public class While extends Activity {
	
	private Predicate mPredicate;
	
	private Activity mDo;
	
	private Activity mEndDo;
	
	public While(Context base, String id) {
		super(base, id);
	}
	
	public void setPredicate(Predicate predicate) {
		mPredicate = predicate;
	}
	
	public void setDo(List<Activity> acts) {
		mDo = acts.get(0);
		mEndDo = acts.get(acts.size() - 1);
	}
	
	@Override
	protected void onCreate() {
		ActivityManager am = ActivityManager.getInstance(this);
		
		// loop
		am.select(mEndDo).onFinish(this);
	}

	@Override
	protected void onStart(Intent intent) {
		boolean result;
		
		try {
			result = mPredicate.eval(intent.getExtras(), boolean.class);
		} catch (Exception e) {
			result = false;
		}
		
		if (result) {
			Intent nextIntent = new Intent(intent.getProcessId());
			nextIntent.setActivity(mDo);
			nextIntent.putExtras(intent.getExtras());
			
			startActivity(nextIntent);
		} else {
			// end while
			finish(intent.getProcessId());
		}
	}
}
