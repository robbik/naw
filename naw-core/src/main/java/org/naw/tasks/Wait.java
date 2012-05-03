package org.naw.tasks;

import java.util.Calendar;

import org.apache.axis.types.DateTime;
import org.apache.axis.types.Duration;
import org.naw.core.task.DataExchange;
import org.naw.core.task.Task;
import org.naw.core.task.TaskContext;

public class Wait implements Task {

	private long deadline;

	private Duration duration;

	public void setDeadline(DateTime deadline) {
		this.deadline = deadline.getCalendar().getTimeInMillis();
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public void run(TaskContext context, DataExchange exchange) throws Exception {
		if (duration == null) {
			context.nextLater(exchange, deadline);
		} else {
			deadline = duration.add(Calendar.getInstance()).getTimeInMillis();

			context.nextLater(exchange, deadline);
		}
	}

	public void recover(TaskContext context, DataExchange exchange) throws Exception {
		run(context, exchange);
	}
}
