package org.naw.core.storage.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.naw.core.exchange.MessageExchange;
import org.naw.core.storage.DefaultStoredTask;
import org.naw.core.storage.Storage;
import org.naw.core.storage.StoredTask;

import rk.commons.logging.Logger;
import rk.commons.logging.LoggerFactory;
import rk.commons.util.ObjectHelper;

public class FileBasedStorage implements Storage {
	
	private static final Logger log = LoggerFactory.getLogger(FileBasedStorage.class);
	
	private File path;
	
	public FileBasedStorage() {
		// do nothing
	}
	
	public void setPath(File path) {
		this.path = path;
	}
	
	private StoredTask read(File file) throws IOException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		try {
			return (StoredTask) ObjectHelper.readObject(in);
		} catch (ClassNotFoundException e) {
			throw new IOException("unknown file format");
		} finally {
			try {
				in.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}
	
	private void write(File f, StoredTask stored) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));;
		
		try {
			ObjectHelper.writeObject(stored, out);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Throwable t) {
					// do nothing
				}
			}
		}
	}

	public Collection<StoredTask> getTasks() {
		List<StoredTask> list = new ArrayList<StoredTask>();
		
		File[] files = path.listFiles();
		
		for (int i = 0, n = files.length; i < n; ++i) {
			File f = files[i];
			
			if (f.isFile() && f.canRead() && f.getName().endsWith(".task")) {
				try {
					list.add(read(f));
				} catch (IOException e) {
					log.error("unable to load file " + f, e);
				}
			}
		}
		
		return list;
	}

	public void persist(String taskId, MessageExchange mex, int status) {
		String fname = taskId.concat("__").concat(mex.getId()).concat(".task");
		
		File f = new File(path, fname);
		
		try {
			write(f, new DefaultStoredTask(taskId, mex, status));
		} catch (IOException e) {
			log.error("unable to persist task " + taskId + ", mex " + mex.getId() +
					" to file " + f, e);
		}
	}

	public void remove(String taskId, String mexId) {
		String fname = taskId.concat("__").concat(mexId).concat(".task");
		
		File f = new File(path, fname);
		
		if (f.canRead()) {
			if (!f.delete()) {
				f.deleteOnExit();
			}
		}
	}

	public void update(String taskId, String mexId, int newStatus) {
		String fname = taskId.concat("__").concat(mexId).concat(".task");
		
		File f = new File(path, fname);
		if (!f.isFile() || !f.canRead() || !f.canWrite()) {
			return;
		}
		
		StoredTask stored;
		
		try {
			stored = read(f);
		} catch (IOException e) {
			log.error("unable to load file " + f, e);
			return;
		}
		
		stored.setStatus(newStatus);
		
		try {
			write(f, stored);
		} catch (IOException e) {
			log.error("unable to update task " + taskId + ", mex " + mexId +
					" to file " + f, e);
		}
	}
}
