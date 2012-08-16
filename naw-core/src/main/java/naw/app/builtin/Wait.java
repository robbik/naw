package naw.app.builtin;

import java.util.Calendar;

import org.apache.axis.types.DateTime;
import org.apache.axis.types.Duration;

import naw.app.Activity;
import naw.content.Context;
import naw.content.Intent;

public class Wait extends Activity {

	private long mDeadline;

	private Duration mDuration;

	public void setDeadline(DateTime deadline) {
		mDeadline = deadline.getCalendar().getTimeInMillis();
	}

	public void setDuration(Duration duration) {
		mDuration = duration;
	}

	public Wait(Context base, String id) {
		super(base, id);
	}

	@Override
	protected void onStart(Intent intent) {
		long deadline;
		
		if (mDuration == null) {
			deadline = mDeadline;
		} else {
			deadline = mDuration.add(Calendar.getInstance()).getTimeInMillis();
		}
		
		finish(intent.getProcessId(), deadline);
	}
}
