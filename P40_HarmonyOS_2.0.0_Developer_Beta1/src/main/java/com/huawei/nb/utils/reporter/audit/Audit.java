package com.huawei.nb.utils.reporter.audit;

import com.huawei.android.util.IMonitorEx;

public abstract class Audit {
    private int auditId;

    public abstract IMonitorEx.EventStreamEx createEventStream();

    protected Audit(int i) {
        this.auditId = i;
    }

    public int getAuditId() {
        return this.auditId;
    }
}
