package org.naw.tasks;

import java.util.Calendar;

import org.apache.axis.types.DateTime;
import org.apache.axis.types.Duration;
import org.naw.core.task.DataExchange;
import org.naw.core.task.LifeCycleAware;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;
import org.naw.core.task.support.Timeout;
import org.naw.core.task.support.Timer;
import org.naw.core.task.support.TimerTask;

public class Wait implements Task, LifeCycleAware {
	
	private Timer timer;

	private long deadline;

	private Duration duration;

	public void setDeadline(DateTime deadline) {
		this.deadline = deadline.getCalendar().getTimeInMillis();
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public void beforeAdd(TaskContext ctx) {
		timer = ctx.getPipeline().getEngine().getTimer();
	}

	public void run(final TaskContext context, final DataExchange exchange) throws Exception {
		if (duration == null) {
			timer.newTimeout(new TimerTask() {

				public void run(Timeout timeout) throws Exception {
					context.forward(exchange);
				}
				
			}, deadline);
		} else {
			timer.newTimeout(new TimerTask() {

				public void run(Timeout timeout) throws Exception {
					context.forward(exchange);
				}
				
			}, duration.add(Calendar.getInstance()).getTimeInMillis());
		}
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		run(context, exchange);
	}
}
