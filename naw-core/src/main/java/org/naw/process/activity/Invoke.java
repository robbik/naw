package org.naw.process.activity;

import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.partnerLink.MessageEvent;
import org.naw.partnerLink.PartnerLink;
import org.naw.partnerLink.PartnerLinkListener;
import org.naw.process.Process;
import org.naw.process.ProcessState;

public class Invoke implements Activity, PartnerLinkListener {

    private final String name;

    private String partnerLink;

    private String operation;

    private boolean oneWay;

    private String requestVar;

    private String responseVar;

    private PartnerLink link;

    private ActivityContext ctx;

    private final AtomicBoolean destroyed;

    public Invoke(String name) {
        this.name = name;

        destroyed = new AtomicBoolean(false);
    }

    public String getName() {
        return name;
    }

    public void setPartnerLink(String partnerLink) {
        this.partnerLink = partnerLink;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setRequestVariable(String requestVar) {
        this.requestVar = requestVar;
    }

    public void setResponseVariable(String responseVar) {
        this.responseVar = responseVar;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public void init(ActivityContext ctx) throws Exception {
        link = ctx.getProcessContext().getPartnerLink(partnerLink);
        if (link == null) {
            throw new IllegalArgumentException("partner link " + partnerLink + " cannot be found");
        }

        this.ctx = ctx;

        link.subscribe(operation + "_callback", this);
    }

    public void messageReceived(MessageEvent e) {
        if (oneWay) {
            return;
        }

        Process process = ctx.getProcessContext().getProcess(e.getDestination());

        if (process != null) {
            if (process.compareAndSet(ProcessState.BEFORE_ACTIVITY, this, ProcessState.AFTER_ACTIVITY, this)) {
                process.getMessage().set(responseVar, e.getValues());

                ctx.execute(process);
            }
        }
    }

    public void execute(Process process) throws Exception {
        link.publish(operation, process.getProcessId(), process.getMessage().get(requestVar));

        if (oneWay) {
            ctx.execute(process);
        }
    }

    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

        // unsubscribe from partner link
        link.unsubscribe(operation + "_callback", this);

        // let gc do its work
        link = null;
        ctx = null;
    }
}
