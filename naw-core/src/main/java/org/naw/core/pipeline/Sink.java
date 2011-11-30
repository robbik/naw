package org.naw.core.pipeline;

import org.naw.core.Process;

public interface Sink {

    void sunk(Pipeline pipeline, Process process);
}
