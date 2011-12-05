package org.naw.core.activity;

import org.naw.core.Process;

/**
 * SCRIPT
 */
public class Script extends AbstractActivity {

	private Handler handler;

	public Script(String name) {
		super(name);
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void execute(Process process) throws Exception {
		handler.handle(process);

		ctx.execute(process);
	}

	public void shutdown() {
		super.shutdown();

		handler = null;
	}

	public static interface Handler {
		void handle(Process process) throws Exception;
	}
}
