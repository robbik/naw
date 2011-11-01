package org.naw.partnerLink;

import java.io.Serializable;
import java.util.Map;

public interface MessageEvent extends Serializable {

    /**
     * Get partner link that received this message
     *
     * @return
     */
    PartnerLink getPartnerLink();

    String getOperation();

    /**
     * Get process id which sent this message
     *
     * @return
     */
    String getSource();

    /**
     * Get process id where this message was sent to
     *
     * @return
     */
    String getDestination();

    /**
     * Get the sent message
     *
     * @return
     */
    Map<String, Object> getValues();

}