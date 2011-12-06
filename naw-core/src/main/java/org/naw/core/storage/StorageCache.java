package org.naw.core.storage;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.naw.core.Process;

public class StorageCache implements Storage {

	private final Storage storage;

	private final Map<String, Process> cache;

	public StorageCache(Storage storage) {
		this.storage = storage;
		this.cache = Collections
				.synchronizedMap(new WeakHashMap<String, Process>());
	}

	public boolean persist(Process process) {
		if (storage.persist(process)) {
			cache.put(process.getId(), process);

			return true;
		}

		return false;
	}

	public void remove(Process process) {
		String pid = process.getId();
		
		storage.remove(process);
		cache.remove(pid);
	}

	public Process find(String pid) {
		Process proc = cache.get(pid);

		if (proc == null) {
			proc = storage.find(pid);

			if (proc != null) {
				cache.put(pid, proc);
			}
		}

		return proc;
	}

	public Process[] findByProcessContext(String contextName) {
		return storage.findByProcessContext(contextName);
	}

	public Process[] findAll() {
		return storage.findAll();
	}
}
