package org.naw.tasks;

import java.util.Map;

import org.naw.core.exchange.MessageExchange;
import org.naw.core.task.TaskContext;

public class Merge extends AbstractTask {

	private String from;

	private String to;

	public void setFrom(String from) {
		this.from = from;
	}

	public void setTo(String to) {
		this.to = to;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run(TaskContext context, MessageExchange exchange) throws Exception {
		Object vfrom = exchange.get(from);
		Object vto = exchange.get(to);

		if ((vfrom instanceof Map<?, ?>) && (vto instanceof Map<?, ?>)) {
			((Map) vto).putAll((Map<?, ?>) vfrom);

			vfrom = vto;

			exchange.set(to, vfrom);
		} else if (vfrom != null) {
			exchange.set(to, vfrom);
		}

		context.send(exchange);
	}

	public void recover(TaskContext context, MessageExchange exchange) throws Exception {
		run(context, exchange);
	}
}
