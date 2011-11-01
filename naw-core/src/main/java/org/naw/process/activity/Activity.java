package org.naw.process.activity;

import org.naw.process.Process;

public interface Activity {

    String getName();

    void init(ActivityContext ctx) throws Exception;

    void execute(Process process) throws Exception;

    void destroy();
}
