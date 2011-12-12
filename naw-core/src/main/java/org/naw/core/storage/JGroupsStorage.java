package org.naw.core.storage;

import org.jgroups.JChannel;
import org.naw.core.Process;

public class JGroupsStorage implements Storage {

	public JGroupsStorage(JChannel ch, String clusterName) {
		//
	}

	public boolean persist(Process process) {
		// TODO Auto-generated method stub
		return false;
	}

	public void remove(Process process) {
		// TODO Auto-generated method stub
	}

	public Process find(String pid) {
		// TODO Auto-generated method stub
		return null;
	}

	public Process[] findByProcessContext(String contextName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Process[] findAll() {
		// TODO Auto-generated method stub
		return null;
	}
}
