package org.naw.core.cluster;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgroups.Channel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

public class DistributedHashMap<K, V> extends ReceiverAdapter implements
		Map<K, V> {

	private static final long serialVersionUID = -5468834496899822439L;

	private final HashMap<K, V> backedMap;

	private final Channel channel;

	public DistributedHashMap(Channel ch, String clusterName, long syncTimeout)
			throws Exception {
		backedMap = new HashMap<K, V>();
		channel = ch;

		ch.setReceiver(this);
		ch.setDiscardOwnMessages(true);

		ch.connect(clusterName);

		ch.getState(null, syncTimeout);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void receive(Message msg) {
		Object msgObj = msg.getObject();

		if (msgObj instanceof CommandDHM) {
			CommandDHM cmd = (CommandDHM) msgObj;

			switch (cmd.getCommand()) {
			case CommandDHM.COMMAND_PUT:
				backedMap.put((K) cmd.getKey(), (V) cmd.getValue());
				break;
			case CommandDHM.COMMAND_REMOVE:
				backedMap.remove((K) cmd.getKey());
				break;
			case CommandDHM.COMMAND_CLEAR:
				backedMap.clear();
				break;
			}
		} else if (msgObj instanceof CommandDHM[]) {
			CommandDHM[] cmds = (CommandDHM[]) msgObj;

			for (int i = 0, len = cmds.length; i < len; ++i) {
				CommandDHM cmd = (CommandDHM) msgObj;

				switch (cmd.getCommand()) {
				case CommandDHM.COMMAND_PUT:
					backedMap.put((K) cmd.getKey(), (V) cmd.getValue());
					break;
				case CommandDHM.COMMAND_REMOVE:
					backedMap.remove((K) cmd.getKey());
					break;
				case CommandDHM.COMMAND_CLEAR:
					backedMap.clear();
					break;
				}
			}
		}
	}

	@Override
	public synchronized void getState(OutputStream output) throws Exception {
		ObjectOutputStream out = new ObjectOutputStream(output);

		out.writeObject(backedMap);
		out.flush();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void setState(InputStream input) throws Exception {
		ObjectInputStream in = new ObjectInputStream(input);
		HashMap<K, V> readMap = (HashMap<K, V>) in.readObject();

		readMap.clear();
		readMap.putAll(readMap);
	}

	public synchronized void clear() {
		try {
			channel.send(new Message(null, null, new CommandDHM(
					CommandDHM.COMMAND_CLEAR, null, null)));
		} catch (Exception e) {
			throw new RuntimeException("unable to send message to cluster", e);
		}

		backedMap.clear();
	}

	public synchronized boolean containsKey(Object key) {
		return backedMap.containsKey(key);
	}

	public synchronized boolean containsValue(Object value) {
		return backedMap.containsValue(value);
	}

	public synchronized Set<java.util.Map.Entry<K, V>> entrySet() {
		return backedMap.entrySet();
	}

	public synchronized V get(Object key) {
		return backedMap.get(key);
	}

	public synchronized boolean isEmpty() {
		return backedMap.isEmpty();
	}

	public synchronized Set<K> keySet() {
		return backedMap.keySet();
	}

	public synchronized V put(K key, V value) {
		try {
			channel.send(new Message(null, null, new CommandDHM(
					CommandDHM.COMMAND_PUT, key, value)));
		} catch (Exception e) {
			throw new RuntimeException("unable to send message to cluster", e);
		}

		return backedMap.put(key, value);
	}

	public synchronized void putAll(Map<? extends K, ? extends V> m) {
		List<CommandDHM> cmds = new ArrayList<CommandDHM>();

		for (Map.Entry<?, ?> entry : m.entrySet()) {
			cmds.add(new CommandDHM(CommandDHM.COMMAND_PUT, entry.getKey(),
					entry.getValue()));
		}

		try {
			channel.send(new Message(null, null, cmds.toArray()));
		} catch (Exception e) {
			throw new RuntimeException("unable to send message to cluster", e);
		}

		backedMap.putAll(m);
	}

	public synchronized V remove(Object key) {
		try {
			channel.send(new Message(null, null, new CommandDHM(
					CommandDHM.COMMAND_REMOVE, key, null)));
		} catch (Exception e) {
			throw new RuntimeException("unable to send message to cluster", e);
		}

		return backedMap.remove(key);
	}

	public synchronized int size() {
		return backedMap.size();
	}

	public synchronized Collection<V> values() {
		return backedMap.values();
	}

	private static class CommandDHM implements Serializable {
		private static final long serialVersionUID = -1771696578032803618L;

		public static final int COMMAND_PUT = 0x01;

		public static final int COMMAND_CLEAR = 0x02;

		public static final int COMMAND_REMOVE = 0x03;

		private final int command;

		private final Object key;

		private final Object value;

		private CommandDHM(int command, Object key, Object value) {
			this.command = command;

			this.key = key;
			this.value = value;
		}

		public int getCommand() {
			return command;
		}

		public Object getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}
	}
}
