package org.naw.core.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.naw.core.Process;
import org.naw.core.logging.Logger;
import org.naw.core.logging.LoggerFactory;

public class FileStorage implements Storage {

	private static final Logger log = LoggerFactory
			.getLogger(FileStorage.class);

	private final File base;

	public FileStorage(File base) {
		this.base = base;

		if (!base.isDirectory()) {
			if (!base.mkdirs()) {
				throw new SecurityException("unable to create directories "
						+ base);
			}
		}
	}

	public FileStorage(String base) {
		this(new File(base));
	}

	public boolean persist(Process process) {
		String pid = process.getId();

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
			if (out != null) {
				try {
					out.close();
				} catch (Throwable t) {
					// do nothing
				}
			}
		}

		return true;
	}

	public void remove(String pid) {
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
	}

	private Process load(String pid, File file) {
		FileInputStream in = null;

		try {
			in = new FileInputStream(file);

			ObjectInputStream ois = new ObjectInputStream(in);
			return (Process) ois.readObject();
		} catch (Throwable t) {
			log.error("unable to read saved process " + pid, t);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Throwable t) {
					// do nothing
				}
			}
		}

		return null;
	}

	public Process find(String pid) {
		File subdir = new File(base, pid.substring(0, 2));

		if (subdir.isDirectory()) {
			File name = new File(subdir, pid.substring(2));

			if (name.canRead()) {
				return load(pid, new File(subdir, pid.substring(2)));
			}
		}

		return null;
	}

	private void findAll(File subdir, List<Process> procs) {
		File[] files = subdir.listFiles();
		if (files == null) {
			return;
		}

		String pidPrefix = subdir.getName();

		for (int i = 0, len = files.length; i < len; ++i) {
			File f = files[i];

			if (f.canRead()) {
				Process proc = load(pidPrefix + f.getName(), f);

				if (proc != null) {
					procs.add(proc);
				}
			}
		}
	}

	public Process[] findAll() {
		List<Process> procs = new ArrayList<Process>();
		File[] files = base.listFiles();

		if (files != null) {
			for (int i = 0, len = files.length; i < len; ++i) {
				File file = files[i];

				if (file.isDirectory()) {
					findAll(file, procs);
				}
			}
		}

		return procs.toArray(new Process[0]);
	}
}
