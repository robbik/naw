package org.naw.partnerLink;

import java.util.Map;

public interface PartnerLink {

    void subscribe(String operation, PartnerLinkListener listener);

    void publish(String operation, String source, Map<String, Object> values);

    void send(String operation, String source, String destination, Map<String, Object> values);

    void unsubscribe(String operation, PartnerLinkListener listener);
}
