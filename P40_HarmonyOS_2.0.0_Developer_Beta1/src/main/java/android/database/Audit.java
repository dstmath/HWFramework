package android.database;

import com.huawei.android.util.IMonitorExt;

public abstract class Audit {
    private int auditId;

    public abstract IMonitorExt.EventStreamExt createEventStream();

    protected Audit(int auditId2) {
        this.auditId = auditId2;
    }

    public int getAuditId() {
        return this.auditId;
    }
}
