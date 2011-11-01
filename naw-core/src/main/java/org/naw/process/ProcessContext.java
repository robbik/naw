package org.naw.process;

import java.util.Collection;

import org.naw.exchange.Message;
import org.naw.partnerLink.PartnerLink;
import org.naw.storage.DehydrationStorage;
import org.naw.util.Timer;

public interface ProcessContext {

    String getName();

    void setDehydrationStorage(DehydrationStorage storage);

    Timer getTimer();

    void init() throws Exception;

    void destroy();

    PartnerLink getPartnerLink(String name);

    Process newProcess();

    Process newProcess(Message message);

    Process getProcess(String pid);

    Collection<Process> getProcesses();

    void hydrateAll();

    void hydrate(String pid);

    void dehydrate(String pid);

    void terminate(String pid);
}
