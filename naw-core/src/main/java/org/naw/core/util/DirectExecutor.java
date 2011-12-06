package org.naw.core.util;

import java.util.concurrent.Executor;

public class DirectExecutor implements Executor {
	
	public DirectExecutor() {
		// do nothing
	}

	public void execute(Runnable command) {
		command.run();
	}
}
