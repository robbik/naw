package org.naw.core.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.naw.core.partnerLink.DefaultMessageEvent;
import org.naw.core.partnerLink.MessageEvent;
import org.naw.core.partnerLink.PartnerLink;
import org.naw.core.partnerLink.PartnerLinkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockPartnerLink implements PartnerLink {

	private static final Logger log = LoggerFactory
			.getLogger(MockPartnerLink.class);

	private Map<String, List<PartnerLinkListener>> listeners;

	private ExecutorService executorService;

	private Lock slock;

	private Lock xlock;

	public MockPartnerLink() {
		listeners = new HashMap<String, List<PartnerLinkListener>>();
		executorService = Executors.newCachedThreadPool();

		ReentrantReadWriteLock sxlock = new ReentrantReadWriteLock(true);
		slock = sxlock.readLock();
		xlock = sxlock.writeLock();
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public void subscribe(String operation, PartnerLinkListener listener) {
		xlock.lock();

		if (listeners.containsKey(operation)) {
			listeners.get(operation).add(listener);
		} else {
			List<PartnerLinkListener> list = new ArrayList<PartnerLinkListener>();
			list.add(listener);

			listeners.put(operation, list);
		}

		xlock.unlock();
	}

	public void unsubscribe(String operation, PartnerLinkListener listener) {
		xlock.lock();

		if (listeners.containsKey(operation)) {
			List<PartnerLinkListener> list = listeners.get(operation);
			list.remove(listener);

			if (list.isEmpty()) {
				listeners.remove(operation);
			}
		}

		xlock.unlock();
	}

	public boolean subscribed(String operation, PartnerLinkListener listener) {
		boolean found = false;
		slock.lock();

		if (listeners.containsKey(operation)) {
			found = listeners.get(operation).contains(listener);
		}

		slock.unlock();
		return found;
	}

	public void publish(String source, String operation,
			Map<String, Object> message) {
		send(source, null, operation, message);
	}

	public void send(String source, String destination, String operation,
			Map<String, Object> message) {
		MessageEvent e = new DefaultMessageEvent(this, source, destination,
				operation, message);

		slock.lock();

		try {
			if (listeners.containsKey(operation)) {
				List<PartnerLinkListener> list = listeners.get(operation);

				int len = list.size();

				for (int i = 0; i < len; ++i) {
					fireMessageReceived(list.get(i), e);
				}
			}
		} finally {
			slock.unlock();
		}
	}

	private void fireMessageReceived(PartnerLinkListener listener,
			MessageEvent e) {
		executorService.submit(new Task(listener, e));
	}

	private static final class Task implements Runnable {

		private PartnerLinkListener listener;

		private MessageEvent e;

		public Task(PartnerLinkListener listener, MessageEvent e) {
			this.listener = listener;
			this.e = e;
		}

		public void run() {
			try {
				listener.messageReceived(e);
			} catch (Throwable t) {
				log.error(
						"unable to invoke messageReceived method on listener "
								+ listener + ".", t);
			}
		}
	}
}
