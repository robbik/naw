package org.naw.engine.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.naw.engine.NawProcess;

public class InMemoryStorage implements Storage {

	// <pid, blob>
	private Map<String, byte[]> disk;

	// <pid, context-name>
	private Map<String, String> idx1;

	public InMemoryStorage() {
		disk = Collections.synchronizedMap(new HashMap<String, byte[]>());
		idx1 = new HashMap<String, String>();
	}

	public boolean persist(NawProcess process) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(process);
			oos.flush();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

		String pid = process.getId();

		synchronized (disk) {
			disk.put(pid, out.toByteArray());
			idx1.put(pid, process.getProcessName());
		}

		return true;
	}

	public void remove(ProcessInstance process) {
		String pid = process.getId();

		synchronized (disk) {
			disk.remove(pid);
			idx1.remove(pid);
		}
	}

	public ProcessInstance find(String pid) {
		byte[] bytes = disk.get(pid);
		if (bytes == null) {
			return null;
		}

		try {
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(bytes));

			return (ProcessInstance) ois.readObject();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public ProcessInstance[] findByProcessContext(String contextName) {
		List<ProcessInstance> procs = new ArrayList<ProcessInstance>();

		synchronized (disk) {
			for (Map.Entry<String, String> e : idx1.entrySet()) {
				if (!contextName.equals(e.getValue())) {
					continue;
				}

				ProcessInstance proc = find(e.getKey());
				if (proc != null) {
					procs.add(proc);
				}
			}
		}

		return procs.toArray(new ProcessInstance[0]);
	}

	public ProcessInstance[] findAll() {
		List<ProcessInstance> procs = new ArrayList<ProcessInstance>();

		synchronized (disk) {
			for (String pid : disk.keySet()) {
				ProcessInstance proc = find(pid);

				if (proc != null) {
					procs.add(proc);
				}
			}
		}

		return procs.toArray(new ProcessInstance[0]);
	}
}
