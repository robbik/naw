package org.naw.process.activity;

import org.naw.process.Process;
import org.naw.process.ProcessContext;
import org.naw.process.pipeline.Pipeline;

public interface ActivityContext {

    Pipeline getPipeline();

    ProcessContext getProcessContext();

    Activity getActivity();

    void execute(Process process);
}
