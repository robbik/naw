package org.naw.core.activity;

import org.naw.core.Process;

public abstract class AbstractActivity implements Activity {

	protected final String name;

	protected ActivityContext ctx;

	protected AbstractActivity(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void init(ActivityContext ctx) throws Exception {
		this.ctx = ctx;
	}

	public abstract void execute(Process process) throws Exception;

	public void destroy() {
		ctx = null;
	}
}
