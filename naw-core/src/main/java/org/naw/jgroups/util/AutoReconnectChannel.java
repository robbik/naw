package org.naw.jgroups.util;

import org.jgroups.Channel;
import org.jgroups.ChannelListener;

public class AutoReconnectChannel implements ChannelListener {

	private final Channel channel;

	private volatile boolean channelClosed;

	private volatile boolean channelConnected;

	public AutoReconnectChannel(Channel channel) {
		this.channel = channel;

		channelClosed = !channel.isOpen();
		channelConnected = channel.isConnected();
	}

	public void channelClosed(Channel channel) {
		if (this.channel.equals(channel)) {
			channelClosed = true;
		}
	}

	public void channelConnected(Channel channel) {
		if (this.channel.equals(channel)) {
			channelConnected = true;
		}
	}

	public void channelDisconnected(Channel channel) {
		if (this.channel.equals(channel)) {
			channelConnected = false;
		}
	}
}
