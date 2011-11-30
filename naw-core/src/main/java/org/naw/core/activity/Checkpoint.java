package org.naw.core.activity;

import org.naw.core.Process;
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

		storage.persist(process);
	}
}
