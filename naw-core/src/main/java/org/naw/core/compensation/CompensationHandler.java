package org.naw.core.compensation;

import org.naw.core.Process;

public interface CompensationHandler {

    void compensate(Process process, Throwable error);
}
