package com.android.server.job.controllers;

import java.util.List;

public abstract class AbsStateController {
    public boolean proxyServiceLocked(int type, List<String> list) {
        return false;
    }

    boolean maybeProxyServiceLocked(JobStatus job) {
        return false;
    }
}
