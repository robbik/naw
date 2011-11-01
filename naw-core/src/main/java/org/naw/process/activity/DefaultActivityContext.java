package org.naw.process.activity;

import org.naw.process.Process;
import org.naw.process.ProcessContext;
import org.naw.process.ProcessState;
import org.naw.process.pipeline.Pipeline;
import org.naw.process.pipeline.Sink;

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
        process.setState(ProcessState.AFTER_ACTIVITY, activity);

        if (next == null) {
            Sink sink = pipeline.getSink();

            if (sink != null) {
                sink.sunk(pipeline, process);
            }
        } else {
            Activity act = next.activity;

            process.setState(ProcessState.BEFORE_ACTIVITY, act);
            try {
                act.execute(process);
            } catch (Exception ex) {
                process.setState(ProcessState.ERROR, act);

                pipeline.exceptionThrown(process, ex, false);
            }
        }
    }
}
