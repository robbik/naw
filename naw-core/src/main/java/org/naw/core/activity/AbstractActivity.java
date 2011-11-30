package org.naw.core.activity;

import org.naw.core.Process;
import org.naw.core.ProcessContext;

public abstract class AbstractActivity implements Activity {

	protected final String name;

	protected ActivityContext ctx;

	protected ProcessContext procctx;

	protected AbstractActivity(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void init(ActivityContext ctx) throws Exception {
		this.ctx = ctx;
		this.procctx = ctx.getProcessContext();
	}

	public abstract void execute(Process process) throws Exception;

	public void destroy() {
		procctx = null;
		ctx = null;
	}
}
