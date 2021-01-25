package com.huawei.nb.utils.reporter.audit;

import com.huawei.android.util.IMonitorEx;
import com.huawei.nb.utils.reporter.Reporter;

public final class DataBaseAccessAudit extends Audit {
    private static final int EVENT_ID = 942010005;
    private static final short PARAM_ID_DATABASE = 1;
    private static final short PARAM_ID_DETAIL = 2;
    private static final short PARAM_ID_INTERVAL = 0;
    private final String database;
    private final String detail;
    private final long interval;

    private DataBaseAccessAudit(String str, String str2, long j) {
        super(EVENT_ID);
        this.database = str;
        this.detail = str2;
        this.interval = j;
    }

    @Override // com.huawei.nb.utils.reporter.audit.Audit
    public IMonitorEx.EventStreamEx createEventStream() {
        IMonitorEx.EventStreamEx openEventStream = IMonitorEx.openEventStream((int) EVENT_ID);
        if (openEventStream != null) {
            openEventStream.setParam(openEventStream, 0, this.interval);
            openEventStream.setParam(openEventStream, (short) PARAM_ID_DATABASE, this.database);
            openEventStream.setParam(openEventStream, (short) PARAM_ID_DETAIL, this.detail);
        }
        return openEventStream;
    }

    public static void report(String str, String str2, long j) {
        Reporter.a(new DataBaseAccessAudit(str, str2, j));
    }
}
