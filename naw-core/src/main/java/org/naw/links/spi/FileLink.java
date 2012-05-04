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
	
	public Object send(Object data, boolean oneWay) throws Exception {
		File file;
		
		if (oneWay) {
			file = this.file;
		} else {
			file = File.createTempFile("FileLink-", ".tmp");
		}
		
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
		
		if (oneWay) {
			return null;
		} else {
			return file;
		}
	}

	public Object receive(Object correlation) throws Exception {
		File file;
		
		if (correlation == null) {
			file = this.file;
		} else {
			file = (File) correlation;
		}
		
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
}
