package org.naw.links.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.naw.exceptions.LinkException;
import org.naw.links.AsyncCallback;
import org.naw.links.Link;
import org.naw.links.LinkAsyncResult;
import org.naw.links.Message;

import rk.commons.util.ObjectUtils;

public class FileLink implements Link {
	
	private File file;
	
	public FileLink(String pathname) {
		file = new File(pathname);
	}
	
	public String getArgument() {
		return file.getAbsolutePath();
	}
	
	public void send(Message msg) throws Exception {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		
		try {
			ObjectUtils.writeObject(msg.getBody(), out);
			
			out.flush();
		} finally {
			try {
				out.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}
	
	public void sendReply(Message msg) throws Exception {
		send(msg);
	}

	public LinkAsyncResult asyncReceive(final Object correlation, Object attachment, long deadline, final AsyncCallback<Message> callback) throws Exception {
		final LinkAsyncResult result = new LinkAsyncResult(this, attachment);
		
		new Thread(new Runnable() {
			
			public void run() {
				ObjectInputStream in = null;
				
				boolean error = true;
				
				try {
					in = new ObjectInputStream(new FileInputStream(FileLink.this.file));
					
					error = false;
				} catch (FileNotFoundException e) {
					result.setFailure(new LinkException(FileLink.this, -900, e.getMessage(), e));
				} catch (IOException e) {
					result.setFailure(new LinkException(FileLink.this, -500, e.getMessage(), e));
				}
				
				if (!error) {
					try {
						result.setSuccess(new Message(correlation, ObjectUtils.readObject(in)));
					} catch (IOException e) {
						result.setFailure(new LinkException(FileLink.this, -500, e.getMessage(), e));
					} catch (Exception e) {
						result.setFailure(new LinkException(FileLink.this, -100, e.getMessage(), e));
					} finally {
						try {
							in.close();
						} catch (Throwable t) {
							// do nothing
						}
					}
				}
				
				callback.completed(result);
			}
		}).start();
		
		return result;
	}
	
	public LinkAsyncResult asyncReceiveReply(Object correlation, Object attachment, long deadline, AsyncCallback<Message> callback) throws Exception {
		return asyncReceive(correlation, attachment, deadline, callback);
	}
}
