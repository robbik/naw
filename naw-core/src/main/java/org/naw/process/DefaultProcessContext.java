package org.naw.process;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.exchange.Message;
import org.naw.partnerLink.PartnerLink;
import org.naw.process.activity.Activity;
import org.naw.process.pipeline.DefaultPipeline;
import org.naw.process.pipeline.Pipeline;
import org.naw.process.pipeline.Sink;
import org.naw.storage.DehydrationStorage;
import org.naw.util.Timer;

public class DefaultProcessContext implements ProcessContext, Sink {

    private final String name;

    private final Map<String, PartnerLink> links;

    private final Map<String, Process> instances;

    private final AtomicBoolean destroyed;

    private DehydrationStorage storage;

    private Timer timer;

    private DefaultPipeline pipeline;

    public DefaultProcessContext(String name) {
        this.name = name;

        pipeline = null;

        links = new ConcurrentHashMap<String, PartnerLink>();
        instances = Collections.synchronizedMap(new HashMap<String, Process>());

        destroyed = new AtomicBoolean(false);
    }

    public String getName() {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        return name;
    }

    public void setDehydrationStorage(DehydrationStorage storage) {
        this.storage = storage;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public Timer getTimer() {
        return timer;
    }

    public void addPartnerLink(String name, PartnerLink link) {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        links.put(name, link);
    }

    public void removePartnerLink(String name) {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        links.remove(name);
    }

    public PartnerLink getPartnerLink(String name) {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        return links.get(name);
    }

    public void setActivities(Activity... activities) {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        pipeline = new DefaultPipeline();
        pipeline.setActivities(activities);
        pipeline.setProcessContext(this);
        pipeline.setSink(this);
    }

    public Process newProcess() {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        DefaultProcess process = new DefaultProcess(this);

        instances.put(process.getProcessId(), process);
        return process;
    }

    public Process newProcess(Message message) {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        DefaultProcess process = new DefaultProcess(this, message);

        instances.put(process.getProcessId(), process);
        return process;
    }

    public Process getProcess(String pid) {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        return instances.get(pid);
    }

    public Collection<Process> getProcesses() {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        return Collections.unmodifiableCollection(instances.values());
    }

    public void hydrateAll() {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        if (storage == null) {
            throw new IllegalStateException("no dehydration storage is defined");
        }

        Process[] processes = storage.retrieveAll(name);
        if (processes != null) {
            for (int i = 0; i < processes.length; ++i) {
                Process process = processes[i];

                if (process.getState() != ProcessState.TERMINATED) {
                    instances.put(process.getProcessId(), process);
                }
            }
        }
    }

    public void hydrate(String pid) {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        if (storage == null) {
            throw new IllegalStateException("no dehydration storage is defined");
        }

        Process process = storage.retrieve(pid);
        if (process != null) {
            instances.put(pid, process);
        }
    }

    public void dehydrate(String pid) {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        if (storage == null) {
            throw new IllegalStateException("no dehydration storage is defined");
        }

        Process process = instances.get(pid);
        if (process != null) {
            storage.store(process);
        }
    }

    public void terminate(String pid) {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        Process process = instances.remove(pid);
        if (process != null) {
            process.terminate();
        }
    }

    public void init() throws Exception {
        if (destroyed.get()) {
            throw new IllegalStateException("process context already destroyed");
        }

        pipeline.init();
    }

    public void sunk(Pipeline pipeline, Process process) {
        terminate(process.getProcessId());
    }

    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

        // destroy instances
        synchronized (instances) {
            for (Process p : instances.values()) {
                p.destroy();
            }

            instances.clear();
        }

        // destroy pipeline
        pipeline.destroy();
        pipeline = null;

        // unlink partner links
        links.clear();

        // unlink dehydration storage
        storage = null;

        // unlink timer
        timer = null;
    }
}
