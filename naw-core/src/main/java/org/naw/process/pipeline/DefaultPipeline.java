package org.naw.process.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.naw.process.Process;
import org.naw.process.ProcessContext;
import org.naw.process.ProcessState;
import org.naw.process.activity.Activity;
import org.naw.process.activity.DefaultActivityContext;
import org.naw.process.compensation.CompensationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPipeline implements Pipeline {

    private static final Logger log = LoggerFactory.getLogger(DefaultPipeline.class);

    private final Pipeline parent;

    private ProcessContext procctx;

    private Sink sink;

    private Activity[] activities;

    private DefaultActivityContext first;

    private final List<CompensationHandler> handlers;

    private final Lock sharedLock;

    private final Lock exclusiveLock;

    public DefaultPipeline() {
        this(null);
    }

    public DefaultPipeline(Pipeline parent) {
        this.parent = parent;

        handlers = new ArrayList<CompensationHandler>();

        ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
        sharedLock = lock.readLock();
        exclusiveLock = lock.writeLock();
    }

    public void setProcessContext(ProcessContext procctx) {
        this.procctx = procctx;
    }

    public ProcessContext getProcessContext() {
        return procctx;
    }

    public void setActivities(Activity... activities) {
        this.activities = activities;
    }

    public void setSink(Sink sink) {
        this.sink = sink;
    }

    public Sink getSink() {
        return sink;
    }

    public Pipeline getParent() {
        return parent;
    }

    public Pipeline init() throws Exception {
        first = null;

        if (activities != null) {
            DefaultActivityContext current = null;

            for (int i = 0; i < activities.length; ++i) {
                DefaultActivityContext newctx = new DefaultActivityContext(this, activities[i]);

                if (current == null) {
                    first = newctx;
                } else {
                    current.setNext(newctx);
                }

                current = newctx;
            }

            current = null;
            activities = null;
        }

        // initialize contexts
        DefaultActivityContext current = first;

        while (current != null) {
            current.getActivity().init(current);
            current = current.getNext();
        }

        current = null;

        return this;
    }

    public void register(CompensationHandler handler) {
        if (handler == null) {
            return;
        }

        exclusiveLock.lock();
        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
        exclusiveLock.unlock();
    }

    public void unregister(CompensationHandler handler) {
        if (handler == null) {
            return;
        }

        exclusiveLock.lock();
        handlers.remove(handler);
        exclusiveLock.unlock();
    }

    public void exceptionThrown(Process process, Throwable cause, boolean rethrown) {
        if (!rethrown) {
            StackTraceElement[] elements = cause.getStackTrace();
            List<StackTraceElement> list = new ArrayList<StackTraceElement>();

            if (elements != null) {
                for (int i = 0; i < elements.length; ++i) {
                    StackTraceElement el = elements[i];

                    String className = el.getClassName();

                    if (!className.startsWith("org.naw.process.activity.")) {
                        list.add(el);
                    }
                }

                cause.setStackTrace(list.toArray(new StackTraceElement[0]));
            }

            log.error("an error occured in process #" + process.getProcessId() + ".\n" + "process dump:\n" + process,
                    cause);
        }

        sharedLock.lock();

        for (int i = handlers.size() - 1; i >= 0; --i) {
            handlers.get(i).compensate(process, cause);
        }

        sharedLock.unlock();

        if (parent != null) {
            parent.exceptionThrown(process, cause, true);
        }
    }

    public Pipeline execute(Process process) {
        if (first == null) {
            if (sink != null) {
                sink.sunk(this, process);
            }
        } else {
            Activity act = first.getActivity();

            process.setState(ProcessState.BEFORE_ACTIVITY, act);
            try {
                act.execute(process);
            } catch (Exception ex) {
                process.setState(ProcessState.ERROR, act);

                exceptionThrown(process, ex, false);
            }
        }

        return this;
    }

    public Pipeline destroy() {
        // destroy activities contexts
        DefaultActivityContext current = first;

        while (current != null) {
            current.getActivity().destroy();
            current = current.getNext();
        }

        current = null;

        // let gc do its work
        procctx = null;
        sink = null;

        activities = null;
        first = null;

        return this;
    }
}
