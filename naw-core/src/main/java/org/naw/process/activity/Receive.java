package org.naw.process.activity;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.naw.partnerLink.MessageEvent;
import org.naw.partnerLink.PartnerLink;
import org.naw.partnerLink.PartnerLinkListener;
import org.naw.process.Process;
import org.naw.process.ProcessState;

public class Receive implements Activity, PartnerLinkListener {

    private final String name;

    private String partnerLink;

    private String operation;

    private String variable;

    private String correlationAttribute;

    private boolean createInstance;

    private boolean oneWay;

    private String attrName;

    private PartnerLink link;

    private ActivityContext ctx;

    private final AtomicBoolean destroyed;

    public Receive(String name) {
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

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public void setCorrelationAttribute(String correlationAttribute) {
        this.correlationAttribute = correlationAttribute;
    }

    public void setCreateInstance(boolean createInstance) {
        this.createInstance = createInstance;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public void init(ActivityContext ctx) throws Exception {
        this.ctx = ctx;

        attrName = "EXCHANGE$" + operation;

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
                || process.compareAndSet(ProcessState.BEFORE_ACTIVITY, this, ProcessState.AFTER_ACTIVITY, this)) {
            if (!oneWay) {
                process.setAttribute(attrName, e.getSource());
            }

            process.getMessage().set(variable, e.getValues());

            ctx.execute(process);
        }
    }

    public void execute(Process process) throws Exception {
        // do nothing
    }

    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

        // unsubscribe from partner link
        link.unsubscribe(operation, this);

        // let gc do its work
        link = null;
        ctx = null;
    }
}
