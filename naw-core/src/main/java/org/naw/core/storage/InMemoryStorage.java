package org.naw.core.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.naw.core.Process;

public class InMemoryStorage implements Storage {

	private Map<String, byte[]> disk;

	public InMemoryStorage() {
		disk = Collections.synchronizedMap(new HashMap<String, byte[]>());
	}

	public boolean persist(Process process) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(process);
			oos.flush();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

		String pid = process.getId();

		disk.put(pid, out.toByteArray());

		return true;
	}

	public void remove(String pid) {
		disk.remove(pid);
	}

	public Process find(String pid) {
		byte[] bytes = disk.get(pid);
		if (bytes == null) {
			return null;
		}

		try {
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(bytes));

			return (Process) ois.readObject();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public Process[] findAll() {
		List<Process> procs = new ArrayList<Process>();

		synchronized (disk) {
			for (String pid : disk.keySet()) {
				Process proc = find(pid);

				if (proc != null) {
					procs.add(proc);
				}
			}
		}

		return procs.toArray(new Process[0]);
	}
}
