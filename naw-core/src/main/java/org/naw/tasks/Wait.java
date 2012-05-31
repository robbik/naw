package org.naw.tasks;

import java.util.Calendar;

import org.apache.axis.types.DateTime;
import org.apache.axis.types.Duration;
import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.LifeCycleAware;
import org.naw.core.task.TaskContext;
import org.naw.core.task.support.Timeout;
import org.naw.core.task.support.Timer;
import org.naw.core.task.support.TimerTask;

public class Wait extends AbstractTask implements LifeCycleAware {
	
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

	public void run(final TaskContext context, final MessageExchange exchange) throws Exception {
		if (duration == null) {
			timer.newTimeout(new TimerTask() {

				public void run(Timeout timeout) throws Exception {
					context.send(exchange);
				}
				
			}, deadline);
		} else {
			timer.newTimeout(new TimerTask() {

				public void run(Timeout timeout) throws Exception {
					context.send(exchange);
				}
				
			}, duration.add(Calendar.getInstance()).getTimeInMillis());
		}
	}

	public void recover(TaskContext context, MessageExchange exchange) throws Exception {
		run(context, exchange);
	}
}
