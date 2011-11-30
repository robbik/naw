package org.naw.core.activity;

import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.core.Process;
import org.naw.core.ProcessState;
import org.naw.core.partnerLink.MessageEvent;
import org.naw.core.partnerLink.PartnerLink;
import org.naw.core.partnerLink.PartnerLinkListener;

public class Invoke extends AbstractActivity implements PartnerLinkListener {

	private String partnerLink;

	private String operation;

	private boolean oneWay;

	private String requestVar;

	private String responseVar;

	private PartnerLink link;

	private final AtomicBoolean destroyed;

	public Invoke(String name) {
		super(name);

		destroyed = new AtomicBoolean(false);
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

	public void init(ActivityContext ctx) throws Exception {
		super.init(ctx);

		link = ctx.getProcessContext().findPartnerLink(partnerLink);
		if (link == null) {
			throw new IllegalArgumentException("partner link " + partnerLink
					+ " cannot be found");
		}

		link.subscribe(operation + "_callback", this);
	}

	public void messageReceived(MessageEvent e) {
		if (oneWay) {
			return;
		}

		Process process = ctx.getProcessContext().findProcess(
				e.getDestination());

		if (process != null) {
			process.getContext().fireProcessEndWait(process, this);

			if (process.compareAndUpdate(ProcessState.BEFORE_ACTIVITY, this,
					ProcessState.AFTER_ACTIVITY, this)) {
				process.getMessage().set(responseVar, e.getMessage());

				ctx.execute(process);
			}
		}
	}

	public void execute(Process process) throws Exception {
		if (!oneWay) {
			process.getContext().fireProcessBeginWait(process, this);
		}

		link.publish(process.getId(), operation,
				process.getMessage().get(requestVar));

		if (oneWay) {
			ctx.execute(process);
		}
	}

	public void destroy() {
		super.destroy();

		if (!destroyed.compareAndSet(false, true)) {
			return;
		}

		// unsubscribe from partner link
		link.unsubscribe(operation + "_callback", this);

		// gc works
		link = null;
	}
}
