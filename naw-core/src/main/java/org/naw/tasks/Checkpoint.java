package org.naw.tasks;

import org.naw.activities.support.AbstractActivity;
import org.naw.engine.NawProcess;
import org.naw.engine.storage.Storage;

/**
 * CHECKPOINT
 */
public class Checkpoint extends AbstractActivity {

	private Storage storage;

	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	public void init() throws Exception {
		if (storage == null) {
			throw new IllegalStateException("storage cannot be null");
		}
	}

	public void execute(NawProcess process, CompletionHandler completion)
			throws Exception {
		storage.persist(process);
		executeNext(process, completion);
	}

	public void recover(NawProcess process, CompletionHandler completion)
			throws Exception {
		executeNext(process, completion);
	}
}
