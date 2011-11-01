package org.naw.process.activity;

import org.naw.partnerLink.PartnerLink;
import org.naw.process.Process;

public class Reply implements Activity {

    private final String name;

    private String partnerLink;

    private String operation;

    private String variable;

    private String attrName;

    private ActivityContext ctx;

    private PartnerLink link;

    public Reply(String name) {
        this.name = name;
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

    public void init(ActivityContext ctx) throws Exception {
        this.ctx = ctx;

        attrName = "EXCHANGE$" + operation;

        link = ctx.getProcessContext().getPartnerLink(partnerLink);
        if (link == null) {
            throw new IllegalArgumentException("partner link " + partnerLink + " cannot be found");
        }
    }

    public void execute(Process process) throws Exception {
        String destination = process.getAttribute(attrName, String.class);
        if (destination != null) {
            link.send(operation + "_callback", process.getProcessId(), destination, process.getMessage().get(variable));
        }

        ctx.execute(process);
    }

    public void destroy() {
        link = null;
        ctx = null;
    }
}
