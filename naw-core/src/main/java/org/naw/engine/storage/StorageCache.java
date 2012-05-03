package org.naw.engine.storage;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.naw.engine.ProcessInstance;

public class StorageCache implements Storage {

	private final Storage storage;

	private final Map<String, ProcessInstance> cache;

	public StorageCache(Storage storage) {
		this.storage = storage;
		this.cache = Collections
				.synchronizedMap(new WeakHashMap<String, ProcessInstance>());
	}

	public boolean persist(ProcessInstance process) {
		if (storage.persist(process)) {
			cache.put(process.getId(), process);

			return true;
		}

		return false;
	}

	public void remove(ProcessInstance process) {
		String pid = process.getId();
		
		storage.remove(process);
		cache.remove(pid);
	}

	public ProcessInstance find(String pid) {
		ProcessInstance proc = cache.get(pid);

		if (proc == null) {
			proc = storage.find(pid);

			if (proc != null) {
				cache.put(pid, proc);
			}
		}

		return proc;
	}

	public ProcessInstance[] findByProcessContext(String contextName) {
		return storage.findByProcessContext(contextName);
	}

	public ProcessInstance[] findAll() {
		return storage.findAll();
	}
}
