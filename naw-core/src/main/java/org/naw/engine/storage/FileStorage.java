package org.naw.engine.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.naw.core.logging.Logger;
import org.naw.core.logging.LoggerFactory;
import org.naw.core.utils.IOUtils;
import org.naw.engine.ProcessInstance;

public class FileStorage implements Storage {

	private static final Logger log = LoggerFactory
			.getLogger(FileStorage.class);

	private final File base;

	private final File refs;

	public FileStorage(File base) {
		this.base = base;
		if (!base.isDirectory()) {
			if (!base.mkdirs()) {
				throw new SecurityException("unable to create directories "
						+ base);
			}
		}

		refs = new File(base, "refs");
		if (!refs.isDirectory()) {
			if (!refs.mkdirs()) {
				throw new SecurityException("unable to create directories "
						+ refs);
			}
		}
	}

	public FileStorage(String base) {
		this(new File(base));
	}

	@SuppressWarnings("unchecked")
	private Set<String> readIndex(String contextName) {
		File file = new File(refs, contextName);
		if (!file.canRead()) {
			return null;
		}

		FileInputStream in = null;

		try {
			in = new FileInputStream(file);

			ObjectInputStream ois = new ObjectInputStream(in);
			return (Set<String>) ois.readObject();
		} catch (Throwable t) {
			log.error("unable to read index " + contextName, t);
		} finally {
			IOUtils.tryClose(in);
		}

		return null;
	}

	private void writeIndex(String contextName, Set<String> idx) {
		File file = new File(refs, contextName);

		if (idx.isEmpty()) {
			if (!file.delete()) {
				file.deleteOnExit();
			}

			return;
		}

		FileOutputStream out = null;

		try {
			out = new FileOutputStream(file);

			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(idx);

			oos.flush();
		} catch (Throwable t) {
			log.error("unable to write index " + contextName, t);
		} finally {
			IOUtils.tryClose(out);
		}
	}

	private ProcessInstance readData(String pid, File file) {
		FileInputStream in = null;

		try {
			in = new FileInputStream(file);

			ObjectInputStream ois = new ObjectInputStream(in);
			return (ProcessInstance) ois.readObject();
		} catch (Throwable t) {
			log.error("unable to read saved process " + pid, t);
		} finally {
			IOUtils.tryClose(in);
		}

		return null;
	}

	public synchronized boolean persist(ProcessInstance process) {
		String pid = process.getId();
		String contextName = process.getProcessName();

		// data file
		File subdir = new File(base, pid.substring(0, 2));
		if (!subdir.isDirectory()) {
			if (!subdir.mkdir()) {
				log.error("unable to create directories " + subdir);
				return false;
			}
		}

		FileOutputStream out = null;

		try {
			out = new FileOutputStream(new File(subdir, pid.substring(2)));

			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(process);
			oos.flush();
		} catch (Throwable t) {
			log.error("unable to persist process " + pid, t);
			return false;
		} finally {
			IOUtils.tryClose(out);
		}

		// index file
		Set<String> idx = readIndex(contextName);
		if (idx == null) {
			idx = new HashSet<String>();
		}

		if (!idx.contains(pid)) {
			idx.add(pid);
			writeIndex(contextName, idx);
		}

		return true;
	}

	public synchronized void remove(ProcessInstance process) {
		String pid = process.getId();
		String contextName = process.getProcessName();

		// data file
		File subdir = new File(base, pid.substring(0, 2));

		if (subdir.isDirectory()) {
			File name = new File(subdir, pid.substring(2));

			if (name.exists()) {
				if (!name.delete()) {
					name.deleteOnExit();
				}
			}

			String[] names = subdir.list();
			if ((names == null) || (names.length == 0)) {
				if (!subdir.delete()) {
					subdir.deleteOnExit();
				}
			}
		}

		// index file
		Set<String> idx = readIndex(contextName);

		if ((idx != null) && idx.contains(pid)) {
			idx.remove(pid);
			writeIndex(contextName, idx);

			String[] names = refs.list();
			if ((names == null) || (names.length == 0)) {
				if (!refs.delete()) {
					refs.deleteOnExit();
				}
			}
		}
	}

	public synchronized ProcessInstance find(String pid) {
		// data file
		File subdir = new File(base, pid.substring(0, 2));

		if (subdir.isDirectory()) {
			File name = new File(subdir, pid.substring(2));

			if (name.canRead()) {
				return readData(pid, new File(subdir, pid.substring(2)));
			}
		}

		return null;
	}

	private synchronized void findAll(File subdir, List<ProcessInstance> procs) {
		// data files
		File[] files = subdir.listFiles();
		if (files == null) {
			return;
		}

		String pidPrefix = subdir.getName();

		for (int i = 0, len = files.length; i < len; ++i) {
			File f = files[i];

			if (f.canRead()) {
				ProcessInstance proc = readData(pidPrefix + f.getName(), f);

				if (proc != null) {
					procs.add(proc);
				}
			}
		}
	}

	public ProcessInstance[] findAll() {
		List<ProcessInstance> procs = new ArrayList<ProcessInstance>();
		File[] files = base.listFiles();

		if (files != null) {
			for (int i = 0, len = files.length; i < len; ++i) {
				File file = files[i];

				if (file.isDirectory()) {
					findAll(file, procs);
				}
			}
		}

		return procs.toArray(new ProcessInstance[0]);
	}

	public ProcessInstance[] findByProcessContext(String contextName) {
		Set<String> pids = readIndex(contextName);
		if ((pids == null) || pids.isEmpty()) {
			return new ProcessInstance[0];
		}

		List<ProcessInstance> list = new ArrayList<ProcessInstance>();

		for (String pid : pids) {
			ProcessInstance proc = find(pid);

			if (proc != null) {
				list.add(proc);
			}
		}

		return list.toArray(new ProcessInstance[0]);
	}
}
