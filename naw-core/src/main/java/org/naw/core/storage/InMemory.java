package org.naw.core.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.naw.core.Engine;
import org.naw.core.Storage;
import org.naw.core.exchange.DefaultMessageExchange;
import org.naw.core.exchange.MessageExchange;
import org.naw.core.utils.ValueGenerators;
import org.naw.executables.Executable;

public class InMemory implements Storage {
	
	private final Map<String, MessageExchange> exchangeMap;
	
	private final Map<String, Set<String>> pendingTaskMap;
	
	private final Map<String, List<String>> onGoingTaskMap;
	
	public InMemory() {
		exchangeMap = new HashMap<String, MessageExchange>();
		
		pendingTaskMap = new HashMap<String, Set<String>>();
		
		onGoingTaskMap = new HashMap<String, List<String>>();
	}

	public synchronized MessageExchange createMessageExchange(Engine engine, Executable executable) {
		String mexId = ValueGenerators.messageExchangeId();
		
		MessageExchange mex = new InMemory.InternalMessageExchange(this, mexId, executable.getName());
		
		exchangeMap.put(mexId, mex);
		pendingTaskMap.put(mexId, new HashSet<String>());
		onGoingTaskMap.put(mexId, new ArrayList<String>());
		
		return mex;
	}

	private synchronized void destroyMessageExchange(String mexId) {
		exchangeMap.remove(mexId);
		pendingTaskMap.remove(mexId);
		onGoingTaskMap.remove(mexId);
	}

	public synchronized void addTask(String mexId, String taskId) {
		Set<String> set = pendingTaskMap.get(mexId);
		if (set != null) {
			set.add(taskId);
		}
	}

	public synchronized void removeTask(String mexId, String taskId) {
		Set<String> set = pendingTaskMap.get(mexId);
		if (set != null) {
			set.remove(taskId);
		}
		
		List<String> list = onGoingTaskMap.get(mexId);
		if (list != null) {
			list.remove(taskId);
		}
	}
	
	public synchronized void addOnGoingTask(String mexId, String taskId) {
		List<String> list = onGoingTaskMap.get(mexId);
		if (list != null) {
			list.add(taskId);
		}
	}

	public synchronized Collection<MessageExchange> getMessageExchanges() {
		return exchangeMap.values();
	}

	public synchronized Collection<String> getTasks(String mexId) {
		Set<String> set = pendingTaskMap.get(mexId);
		if (set == null) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableSet(set);
		}
	}

	public synchronized Collection<String> getOnGoingTasks(String mexId) {
		List<String> list = onGoingTaskMap.get(mexId);
		if (list == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(list);
		}
	}
	
	private static class InternalMessageExchange extends DefaultMessageExchange {

		private static final long serialVersionUID = -278663198561473006L;
		
		private final InMemory parent;

		public InternalMessageExchange(InMemory parent, String id, String executableName) {
			super(id, executableName);
			
			this.parent = parent;
		}

		@Override
		public void destroy() {
			parent.destroyMessageExchange(id);
			
			super.destroy();
		}
	}
}
