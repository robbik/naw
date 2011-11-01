package org.naw.storage;

import org.naw.process.Process;

public interface DehydrationStorage {

    boolean store(Process process);

    Process retrieve(String pid);

    Process[] retrieveAll(String name);
}
