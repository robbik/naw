package org.naw.core.activity;

import org.naw.core.Process;
import org.naw.core.ProcessState;
import org.naw.core.storage.Storage;

/**
 * CHECKPOINT
 */
public class Checkpoint extends AbstractActivity {

	public Checkpoint(String name) {
		super(name);
	}

	public void execute(Process process) throws Exception {
		Storage storage = process.getContext().getStorage();
		if (storage != null) {
			synchronized (process) {
				process.noFireEventUpdate(ProcessState.AFTER, this);
				storage.persist(process);
			}
		}

		ctx.execute(process);
	}
}
