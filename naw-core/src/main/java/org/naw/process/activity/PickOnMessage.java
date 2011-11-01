package org.naw.process.activity;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.partnerLink.MessageEvent;
import org.naw.partnerLink.PartnerLink;
import org.naw.partnerLink.PartnerLinkListener;
import org.naw.process.Process;
import org.naw.process.ProcessState;
import org.naw.process.pipeline.DefaultPipeline;
import org.naw.process.pipeline.Pipeline;
import org.naw.process.pipeline.Sink;

public class PickOnMessage implements PartnerLinkListener, Sink {

    private final Pick parent;

    private ActivityContext ctx;

    private String partnerLink;

    private String operation;

    private String variable;

    private String correlationAttribute;

    private boolean createInstance;

    private boolean oneWay;

    private Activity[] activities;

    private String attrName;

    private PartnerLink link;

    private DefaultPipeline pipeline;

    private final AtomicBoolean destroyed;

    public PickOnMessage(Pick parent) {
        this.parent = parent;

        destroyed = new AtomicBoolean(false);
    }

    public void setPartnerLink(String partnerLink) {
        this.partnerLink = partnerLink;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public void setCorrelationAttribute(String correlationAttribute) {
        this.correlationAttribute = correlationAttribute;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public void setActivities(Activity... activities) {
        this.activities = activities;
    }

    public void init(ActivityContext ctx) throws Exception {
        this.ctx = ctx;

        if (activities == null) {
            pipeline = null;
        } else {
            pipeline = new DefaultPipeline(ctx.getPipeline());
            pipeline.setActivities(activities);
            pipeline.setProcessContext(ctx.getProcessContext());
            pipeline.setSink(this);

            activities = null;
        }

        attrName = "EXCHANGE$" + operation;

        createInstance = parent.isCreateInstance();

        link = ctx.getProcessContext().getPartnerLink(partnerLink);
        if (link == null) {
            throw new IllegalArgumentException("partner link " + partnerLink + " cannot be found");
        }

        link.subscribe(operation, this);
    }

    public void messageReceived(MessageEvent e) {
        Map<String, Object> values = e.getValues();

        Process process = null;

        if (createInstance) {
            process = ctx.getProcessContext().newProcess();
        } else {
            process = ctx.getProcessContext().getProcess((String) values.get(correlationAttribute));
        }

        if (process == null) {
            return;
        }

        if (createInstance
                || process.compareAndSet(ProcessState.BEFORE_ACTIVITY, parent, ProcessState.AFTER_ACTIVITY, parent)) {
            parent.afterExecute(process);

            if (!oneWay) {
                process.setAttribute(attrName, e.getSource());
            }

            process.getMessage().set(variable, e.getValues());

            if (pipeline == null) {
                ctx.execute(process);
            } else {
                pipeline.execute(process);
            }
        }
    }

    public void sunk(Pipeline pipeline, Process process) {
        ctx.execute(process);
    }

    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

        // unlink from partnerLink
        link.unsubscribe(operation, this);

        // destroy pipeline
        if (pipeline != null) {
            pipeline.destroy();
        }

        // gc works
        ctx = null;
        activities = null;
        link = null;
        pipeline = null;
    }
}
