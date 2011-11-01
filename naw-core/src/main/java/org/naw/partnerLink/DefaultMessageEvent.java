package org.naw.partnerLink;

import java.util.Map;

public class DefaultMessageEvent implements MessageEvent {

    private static final long serialVersionUID = -4628669083071464389L;

    private PartnerLink partnerLink;

    private String operation;

    private String source;

    private String destination;

    private Map<String, Object> values;

    public DefaultMessageEvent() {
        // do nothing
    }

    public DefaultMessageEvent(PartnerLink partnerLink, String operation, String source, String destination,
            Map<String, Object> values) {
        this.partnerLink = partnerLink;

        this.operation = operation;
        this.source = source;
        this.destination = destination;

        this.values = values;
    }

    public PartnerLink getPartnerLink() {
        return partnerLink;
    }

    public void setPartnerLink(PartnerLink partnerLink) {
        this.partnerLink = partnerLink;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setMessage(Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return super.toString() + " [\n" + "operation=" + operation + "\nsource=" + source + "\ndestination"
                + destination + "\nvalues=\n" + values + "\n]";
    }
}
