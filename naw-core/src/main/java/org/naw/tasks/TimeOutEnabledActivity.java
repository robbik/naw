package org.naw.tasks;

import java.util.concurrent.TimeUnit;

import org.naw.activities.support.AbstractActivity;
import org.naw.engine.NawProcess;

public abstract class TimeOutEnabledActivity extends AbstractActivity {

	private long duration;

	private String ivarname;

	public void setDuration(long duration, TimeUnit unit) {
		this.duration = unit.toMillis(duration);
	}

	public void init() throws Exception {
		ivarname = super.activityQName.concat(".START_TIME");
	}

	protected long getTimeOutDuration(NawProcess process) {
		if (duration <= 0) {
			return 0;
		}

		long tnow = System.currentTimeMillis();

		Long stime = process.geti(ivarname);
		long stimep;

		if (stime == null) {
			stime = Long.valueOf(tnow);
			process.seti(ivarname, stime);

			stimep = tnow;
		} else {
			stimep = stime.longValue();
		}

		long nextWaitFor = duration - (tnow - stimep);

		if (nextWaitFor <= 0) {
			process.unseti(ivarname);
			return 0;
		}

		return nextWaitFor;
	}
}
