package org.naw.core.activity;

import org.naw.core.Process;
import org.naw.core.partnerLink.PartnerLink;

/**
 * REPLY
 */
public class Reply extends AbstractActivity {

	private String partnerLink;

	private String operation;

	private String variable;

	private String attrName;

	private PartnerLink link;

	public Reply(String name) {
		super(name);
	}

	public void setPartnerLink(String partnerLink) {
		this.partnerLink = partnerLink;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public void init(ActivityContext ctx) throws Exception {
		super.init(ctx);

		attrName = "EXCHANGE$" + operation;

		link = procctx.findPartnerLink(partnerLink);
		if (link == null) {
			throw new IllegalArgumentException("partner link " + partnerLink
					+ " cannot be found");
		}
	}

	public void execute(Process process) throws Exception {
		String destination = process.getAttribute(attrName, String.class);
		if (destination != null) {
			link.send(process.getId(), destination, operation + "_callback",
					process.getMessage().get(variable));
		}

		ctx.execute(process);
	}

	public void shutdown() {
		super.shutdown();

		link = null;
	}
}
