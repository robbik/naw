package org.naw.core.listener;

import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.ProcessState;
import org.naw.core.storage.Storage;

public class AutoSaveProcess extends SimpleLifeCycleListener {

	private Strategy strategy;

	private boolean purgeTerminated;

	public AutoSaveProcess() {
		this.strategy = Strategy.ON_SLEEP;
		this.purgeTerminated = true;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public void setPurgeTerminated(boolean purgeTerminated) {
		this.purgeTerminated = purgeTerminated;
	}

	@Override
	public void processStateChange(ProcessContext ctx, Process process) {
		Storage storage = ctx.getStorage();
		if (storage == null) {
			return;
		}

		switch (strategy) {
		case ON_SLEEP:
			if (process.getState() == ProcessState.SLEEP) {
				storage.persist(process);
			}
			break;
		case EVERY_STATE:
			storage.persist(process);
			break;
		}
	}

	@Override
	public void processTerminated(ProcessContext ctx, Process process) {
		if (purgeTerminated && (strategy != Strategy.TRANSIENT)) {
			Storage storage = ctx.getStorage();
			if (storage == null) {
				return;
			}

			storage.remove(process.getId());
		}
	}

	public static enum Strategy {
		EVERY_STATE, ON_SLEEP, TRANSIENT
	}
}
