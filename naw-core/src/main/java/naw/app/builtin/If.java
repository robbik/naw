package naw.app.builtin;

import java.util.List;

import naw.app.Activity;
import naw.app.ActivityManager;
import naw.content.Context;
import naw.content.Intent;
import naw.content.Predicate;

public class If extends Activity {
	
	private Predicate mPredicate;
	
	private Activity mThen;
	
	private Activity mEndThen;
	
	private Activity mElse;
	
	private Activity mEndElse;
	
	private Activity endIf;

	public If(Context base, String id) {
		super(base, id);
		
		endIf = new Activity(base, id + "____EndIf") {

			@Override
			protected void onStart(Intent intent) {
				onEndIf(intent);
			}
		};
	}

	public void setPredicate(Predicate predicate) {
		mPredicate = predicate;
	}

	public void setThen(List<Activity> then) {
		mThen = then.get(0);
		mEndThen = then.get(then.size() - 1);
	}
	
	public void setElse(List<Activity> _else) {
		mElse = _else.get(0);
		mEndElse = _else.get(_else.size() - 1);
	}

	@Override
	protected void onCreate() {
		ActivityManager am = ActivityManager.getInstance(this);
		
		am.select(mEndThen).onFinish(endIf);
		
		if (mEndElse != null) {
			am.select(mEndElse).onFinish(endIf);
		}
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
			nextIntent.setActivity(mThen);
			nextIntent.putExtras(intent.getExtras());
			
			startActivity(nextIntent);
		} else if (mElse != null) {
			Intent nextIntent = new Intent(intent.getProcessId());
			nextIntent.setActivity(mElse);
			nextIntent.putExtras(intent.getExtras());
			
			startActivity(nextIntent);
		} else {
			onEndIf(intent);
		}
	}
	
	/*package*/ void onEndIf(Intent intent) {
		finish(intent.getProcessId());
	}
}
