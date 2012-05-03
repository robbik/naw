package org.naw.links.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.naw.links.Link;

import rk.commons.util.ObjectUtils;

public class FileLink implements Link {
	
	private File file;
	
	public FileLink(String pathname) {
		file = new File(pathname);
	}
	
	public void send(Object data) throws Exception {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		
		try {
			ObjectUtils.writeBytes(data, out);
			
			out.flush();
		} finally {
			try {
				out.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}

	public Object receive() throws Exception {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		
		try {
			return ObjectUtils.readFromBytes(in);
		} finally {
			try {
				in.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}

	public String sendAndReceive(Object data) throws Exception {
		File file = File.createTempFile("FileLink-", ".tmp");
		
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		
		try {
			ObjectUtils.writeBytes(data, out);
			
			out.flush();
		} finally {
			try {
				out.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
		
		return file.getAbsolutePath();
	}

	public Object receive(String ref) throws Exception {
//		ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(ref)));
//		
//		try {
//			return ObjectUtils.readFromBytes(in);
//		} finally {
//			try {
//				in.close();
//			} catch (Throwable t) {
//				// do nothing
//			}
//		}
		
		return null;
	}
}
