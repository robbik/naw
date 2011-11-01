package org.naw.process.compensation;

import org.naw.process.Process;

public interface CompensationHandler {

    void compensate(Process process, Throwable error);
}
