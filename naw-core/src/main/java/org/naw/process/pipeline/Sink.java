package org.naw.process.pipeline;

import org.naw.process.Process;

public interface Sink {

    void sunk(Pipeline pipeline, Process process);
}
