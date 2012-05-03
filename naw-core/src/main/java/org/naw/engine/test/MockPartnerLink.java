package org.naw.engine.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.naw.links.DefaultMessageEvent;
import org.naw.links.MessageEvent;
import org.naw.links.Link;
import org.naw.links.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockPartnerLink implements Link {

	private static final Logger log = LoggerFactory
			.getLogger(MockPartnerLink.class);

	private Map<String, List<PartnerLinkListener>> listeners;

	private Executor executor;

	private Lock slock;

	private Lock xlock;

	public MockPartnerLink() {
		listeners = new HashMap<String, List<PartnerLinkListener>>();
		executor = Executors.newCachedThreadPool();

		ReentrantReadWriteLock sxlock = new ReentrantReadWriteLock(true);
		slock = sxlock.readLock();
		xlock = sxlock.writeLock();
	}

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
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

	public int subscriptions(String operation) {
		int found;
		slock.lock();

		if (listeners.containsKey(operation)) {
			found = listeners.get(operation).size();
		} else {
			found = 0;
		}

		slock.unlock();
		return found;
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
		List<PartnerLinkListener> list = listeners.get(operation);

		if (list != null) {
			if (executor == null) {
				for (int i = 0, len = list.size(); i < len; ++i) {
					try {
						list.get(i).messageReceived(e);
					} catch (Throwable t) {
						log.error(
								"unable to invoke messageReceived method on listener "
										+ list.get(i) + ".", t);
					}
				}
			} else {
				for (int i = 0, len = list.size(); i < len; ++i) {
					executor.execute(new Task(list.get(i), e));
				}
			}
		}
		slock.unlock();
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
