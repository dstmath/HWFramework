package com.android.server.rms.handler;

import android.app.mtm.MultiTaskPolicy;

public interface HwSysResHandler {
    boolean execute(MultiTaskPolicy multiTaskPolicy);

    void interrupt();
}
