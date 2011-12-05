package org.naw.core.activity;

import static org.naw.core.ProcessState.AFTER;
import static org.naw.core.ProcessState.BEFORE;
import static org.naw.core.ProcessState.SLEEP;

import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.core.Process;
import org.naw.core.partnerLink.MessageEvent;
import org.naw.core.partnerLink.PartnerLink;
import org.naw.core.partnerLink.PartnerLinkListener;

/**
 * INVOKE
 */
public class Invoke extends AbstractActivity implements PartnerLinkListener {

	private String partnerLink;

	private String operation;

	private boolean oneWay;

	private String requestVar;

	private String responseVar;

	private PartnerLink link;

	private final AtomicBoolean shutdown;

	public Invoke(String name) {
		super(name);

		shutdown = new AtomicBoolean(false);
	}

	public void setPartnerLink(String partnerLink) {
		this.partnerLink = partnerLink;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setRequestVariable(String variable) {
		requestVar = variable;
	}

	public void setResponseVariable(String variable) {
		responseVar = variable;
	}

	public void setOneWay(boolean oneWay) {
		this.oneWay = oneWay;
	}

	@Override
	public void init(ActivityContext ctx) throws Exception {
		super.init(ctx);

		link = procctx.findPartnerLink(partnerLink);
		if (link == null) {
			throw new IllegalArgumentException("partner link " + partnerLink
					+ " cannot be found");
		}

		link.subscribe(operation + "_callback", this);
	}

	public void messageReceived(MessageEvent e) {
		if (oneWay || shutdown.get()) {
			return;
		}

		Process process = procctx.findProcess(e.getDestination());
		if (process == null) {
			return;
		}

		boolean updated;

		synchronized (process) {
			updated = process.compare(BEFORE, this)
					|| process.compare(SLEEP, this);

			if (updated) {
				process.getMessage().set(responseVar, e.getMessage());
				process.update(AFTER);
			}
		}

		if (updated) {
			ctx.execute(process);
		}
	}

	public void execute(Process process) throws Exception {
		link.publish(process.getId(), operation,
				process.getMessage().get(requestVar));

		if (oneWay) {
			ctx.execute(process);
		} else {
			process.compareAndUpdate(BEFORE, this, SLEEP);
		}
	}

	@Override
	public void shutdown() {
		if (!shutdown.compareAndSet(false, true)) {
			return;
		}

		super.shutdown();

		// unsubscribe from partner link
		link.unsubscribe(operation + "_callback", this);

		// gc works
		link = null;
	}
}
