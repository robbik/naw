package org.naw.core.activity;

import org.naw.core.Process;

/**
 * EMPTY
 */
public class Empty extends AbstractActivity {

	public Empty(String name) {
		super(name);
	}

	public void execute(Process process) throws Exception {
		ctx.execute(process);
	}
}
