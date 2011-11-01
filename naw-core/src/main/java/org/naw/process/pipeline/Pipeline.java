package org.naw.process.pipeline;

import org.naw.process.Process;
import org.naw.process.ProcessContext;
import org.naw.process.compensation.CompensationHandler;

public interface Pipeline {

    ProcessContext getProcessContext();

    Sink getSink();

    Pipeline getParent();

    Pipeline init() throws Exception;

    void register(CompensationHandler handler);

    void unregister(CompensationHandler handler);

    void exceptionThrown(Process process, Throwable cause, boolean rethrown);

    Pipeline execute(Process process);

    Pipeline destroy();
}
