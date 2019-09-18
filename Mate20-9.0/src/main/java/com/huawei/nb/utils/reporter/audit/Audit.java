package com.huawei.nb.utils.reporter.audit;

import com.huawei.android.util.IMonitorEx;

public abstract class Audit {
    private int auditId;

    public abstract IMonitorEx.EventStreamEx createEventStream();

    protected Audit(int auditId2) {
        this.auditId = auditId2;
    }

    public int getAuditId() {
        return this.auditId;
    }
}
