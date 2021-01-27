package com.huawei.nb.utils.reporter.audit;

import com.huawei.android.util.IMonitorEx;
import com.huawei.nb.utils.reporter.Reporter;

public class DataBaseSizeAudit extends Audit {
    private static final int EVENT_ID = 942010006;
    private static final short PARAM_ID_DATABASE = 1;
    private static final short PARAM_ID_INTERVAL = 0;
    private static final short PARAM_ID_SIZE = 2;
    private final String database;
    private final long interval;
    private final long size;

    private DataBaseSizeAudit(String str, long j, long j2) {
        super(EVENT_ID);
        this.database = str;
        this.size = j;
        this.interval = j2;
    }

    @Override // com.huawei.nb.utils.reporter.audit.Audit
    public IMonitorEx.EventStreamEx createEventStream() {
        IMonitorEx.EventStreamEx openEventStream = IMonitorEx.openEventStream((int) EVENT_ID);
        if (openEventStream != null) {
            openEventStream.setParam(openEventStream, (short) PARAM_ID_INTERVAL, this.interval);
            openEventStream.setParam(openEventStream, (short) PARAM_ID_DATABASE, this.database);
            openEventStream.setParam(openEventStream, (short) PARAM_ID_SIZE, this.size);
        }
        return openEventStream;
    }

    public static void report(String str, long j, long j2) {
        Reporter.a(new DataBaseSizeAudit(str, j, j2));
    }
}
