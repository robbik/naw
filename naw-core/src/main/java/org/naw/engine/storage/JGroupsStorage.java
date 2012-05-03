package org.naw.engine.storage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jgroups.Channel;
import org.jgroups.ChannelListener;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.naw.engine.ProcessInstance;

public class JGroupsStorage extends ReceiverAdapter implements Storage,
		ChannelListener {

	private final Storage backed;

	private final Channel channel;

	private final String clusterName;

	private volatile long reconnectDelay;

	private volatile boolean channelOpen;

	private volatile boolean channelConnected;

	private final Executor executor;

	private final Runnable rworker = new Runnable() {

		public void run() {
			while (channel.isOpen() && !channel.isConnected()) {
				try {
					Thread.sleep(reconnectDelay);
				} catch (InterruptedException ex) {
					break;
				}
			}
		}
	};

	public JGroupsStorage(Storage backed, Channel channel, String clusterName) {
		this.backed = backed;

		this.channel = channel;
		this.clusterName = clusterName;

		reconnectDelay = 10000L;
		executor = Executors.newCachedThreadPool();

		channel.addChannelListener(this);

		channelOpen = channel.isOpen();
		channelConnected = channel.isConnected();
	}

	public void setReconnectDelay(long reconnectDelay) {
		if (reconnectDelay <= 0) {
			throw new IllegalArgumentException("delay must greater than zero");
		}

		this.reconnectDelay = reconnectDelay;
	}

	public void channelClosed(Channel channel) {
		channelOpen = false;
	}

	public void channelConnected(Channel channel) {
		channelConnected = true;
	}

	public void channelDisconnected(Channel channel) {
		channelConnected = false;
		executor.execute(rworker);
	}

	@Override
	public void getState(OutputStream output) throws Exception {
		// TODO Auto-generated method stub
		super.getState(output);
	}

	@Override
	public void receive(Message msg) {
		// TODO Auto-generated method stub
		super.receive(msg);
	}

	@Override
	public void setState(InputStream input) throws Exception {
		// TODO Auto-generated method stub
		super.setState(input);
	}

	public void init() throws Exception {
		channel.connect(clusterName);
	}

	public void destroy() {
		channel.disconnect();
	}

	public boolean persist(ProcessInstance process) {
		boolean ok = backed.persist(process);

		// TODO propagate to another cluster
		return ok;
	}

	public void remove(ProcessInstance process) {
		backed.remove(process);

		// TODO propagate to another cluster
	}

	public ProcessInstance find(String pid) {
		ProcessInstance process = backed.find(pid);
		if (process == null) {
			// TODO find from another cluster
		}

		return process;
	}

	public ProcessInstance[] findByProcessContext(String contextName) {
		// TODO Auto-generated method stub
		return null;
	}

	public ProcessInstance[] findAll() {
		// TODO Auto-generated method stub
		return null;
	}
}
