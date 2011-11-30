package org.naw.core.activity;

import org.naw.core.Process;

/**
 * NOOP
 */
public class Empty extends AbstractActivity {

	public Empty(String name) {
		super(name);
	}

	public void execute(Process process) throws Exception {
		ctx.execute(process);
	}
}
