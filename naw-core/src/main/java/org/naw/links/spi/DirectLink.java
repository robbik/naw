package org.naw.links.spi;

import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import org.naw.core.task.support.Timeout;
import org.naw.core.task.support.Timer;
import org.naw.core.task.support.TimerTask;
import org.naw.core.utils.SynchronizedHashMap;
import org.naw.exceptions.ErrorCodes;
import org.naw.exceptions.LinkException;
import org.naw.links.AsyncCallback;
import org.naw.links.Link;
import org.naw.links.LinkAsyncResult;
import org.naw.links.Message;

import rk.commons.util.ObjectUtils;

public class DirectLink implements Link {
	
	protected static final int MAX_SPIN = Runtime.getRuntime().availableProcessors();
	
	protected final Timer timer;
	
	protected final long sendTimeout;
	
	protected final Map<Object, DirectLinkReceiver> requests;
	
	protected final Map<Object, DirectLinkReceiver> replies;
	
	protected final String argument;
	
	public DirectLink(Timer timer, long sendTimeout, String argument) {
		this.timer = timer;
		this.sendTimeout = sendTimeout;
		this.argument = argument;
		
		requests = new SynchronizedHashMap<Object, DirectLinkReceiver>();
		replies = new SynchronizedHashMap<Object, DirectLinkReceiver>();
	}
	
	private void dosend(Map<Object, DirectLinkReceiver> map, Message message) throws LinkException, Exception {
		long deadline = System.currentTimeMillis() + sendTimeout;
		int spins = MAX_SPIN;
		
		DirectLinkReceiver receiver = null;
		Object correlation = message.getCorrelation();
		
		do {
			synchronized (map) {
				receiver = map.remove(correlation);
					
				if ((receiver == null) && (correlation != null)) {
					receiver = map.remove(null);
				}
				
				if ((receiver != null) && (receiver.timeout != null)) {
					receiver.timeout.cancel();
				}
			}
			
			if (receiver == null) {
				--spins;
				
				if (spins <= 0) {
					spins = MAX_SPIN;
					
					LockSupport.parkNanos(1000);
				} else {
					Thread.yield();
				}
			}
		} while ((deadline > System.currentTimeMillis()) && (receiver == null));
		
		if (receiver == null) {
			throw new LinkException(this, ErrorCodes.LINK_DOWN,
					"no async-receive performed for no correlation nor " + message.getCorrelation());
		} else {
			receiver.ar.setSuccess(message);
			receiver.callback.completed(receiver.ar);
		}
	}
	
	private LinkAsyncResult doasyncReceive(Map<Object, DirectLinkReceiver> map,
			Object correlation, Object attachment,
			long deadline,
			AsyncCallback<Message> callback) throws Exception {
		
		DirectLinkReceiver receiver;
		
		synchronized (map) {
			receiver = map.get(correlation);
			
			if (receiver == null) {
				receiver = new DirectLinkReceiver();
				
				receiver.ar = new DirectLinkAsyncResult(correlation, attachment);
				receiver.callback = callback;
				
				if (deadline > 0) {
					if (timer == null) {
						throw new IllegalArgumentException("timeout not supported, no timer defined");
					}
					
					receiver.timeout = timer.newTimeout(new DirectLinkTimerTask(receiver.ar, callback), deadline);
				}
				
				map.put(correlation, receiver);
			} else {
				if (!ObjectUtils.equals(receiver.callback, callback)) {
					throw new IllegalArgumentException("async-receive already performed for link " + this + " with correlation " + correlation);
				}
			}
		}
		
		return receiver.ar;
	}
	
	public String getArgument() {
		return argument;
	}
	
	public void send(Message message) throws LinkException, Exception {
		dosend(requests, message);
	}
	
	public LinkAsyncResult asyncReceive(Object correlation, Object attachment, long deadline, AsyncCallback<Message> callback) throws Exception {
		return doasyncReceive(requests, correlation, attachment, deadline, callback);
	}
	
	public void sendReply(Message message) throws LinkException, Exception {
		dosend(replies, message);
	}
	
	public LinkAsyncResult asyncReceiveReply(Object correlation, Object attachment, long deadline, AsyncCallback<Message> callback) throws Exception {
		return doasyncReceive(replies, correlation, attachment, deadline, callback);
	}
	
	class DirectLinkReceiver {
		LinkAsyncResult ar;
		
		AsyncCallback<Message> callback;
		
		Timeout timeout;
	}
	
	class DirectLinkAsyncResult extends LinkAsyncResult {
		
		final Object correlation;
		
		DirectLinkAsyncResult(Object correlation, Object attachment) {
			super(DirectLink.this, attachment);
			
			this.correlation = correlation;
		}
		
		@Override
		public boolean docancel() {
			synchronized (DirectLink.this.requests) {
				DirectLinkReceiver receiver = DirectLink.this.requests.remove(correlation);
				
				if (receiver == null) {
					return false;
				} else {
					if (receiver.timeout != null) {
						receiver.timeout.cancel();
					}
					
					return true;
				}
			}
		}
	}
	
	class DirectLinkTimerTask implements TimerTask {
		
		private final LinkAsyncResult asyncResult;
		
		private final AsyncCallback<Message> callback;
		
		DirectLinkTimerTask(LinkAsyncResult asyncResult, AsyncCallback<Message> callback) {
			this.asyncResult = asyncResult;
			this.callback = callback;
		}
		
		public void run(Timeout timeout) throws Exception {
			if (asyncResult.timeout()) {
				callback.completed(asyncResult);
			}
		}
	}
}