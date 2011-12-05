package org.naw.core.util;

import java.util.concurrent.Executor;

public class DirectExecutor implements Executor {
	
	public static DirectExecutor INSTANCE = new DirectExecutor();
	
	private DirectExecutor() {
		// do nothing
	}

	public void execute(Runnable command) {
		command.run();
	}
}
