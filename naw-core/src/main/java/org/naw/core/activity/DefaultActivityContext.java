package org.naw.core.activity;

import org.naw.core.Process;
import org.naw.core.ProcessContext;
import org.naw.core.ProcessState;
import org.naw.core.pipeline.Pipeline;
import org.naw.core.pipeline.Sink;

public class DefaultActivityContext implements ActivityContext {

    private final Pipeline pipeline;

    private final Activity activity;

    private DefaultActivityContext next;

    public DefaultActivityContext(Pipeline pipeline, Activity activity) {
        this.pipeline = pipeline;
        this.activity = activity;

        next = null;
    }

    public void setNext(DefaultActivityContext next) {
        this.next = next;
    }

    public DefaultActivityContext getNext() {
        return next;
    }

    public void unsetNext() {
        next = null;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public ProcessContext getProcessContext() {
        return pipeline.getProcessContext();
    }

    public Activity getActivity() {
        return activity;
    }

    public void execute(Process process) {
        process.update(ProcessState.AFTER_ACTIVITY, activity);

        if (next == null) {
            Sink sink = pipeline.getSink();

            if (sink != null) {
                sink.sunk(pipeline, process);
            }
        } else {
            Activity act = next.activity;

            process.update(ProcessState.BEFORE_ACTIVITY, act);
            try {
                act.execute(process);
            } catch (Throwable t) {
                process.update(ProcessState.ERROR, act);

                pipeline.exceptionThrown(process, t, false);
            }
        }
    }
}
