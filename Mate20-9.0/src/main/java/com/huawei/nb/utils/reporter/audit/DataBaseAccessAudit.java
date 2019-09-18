package com.huawei.nb.utils.reporter.audit;

import com.huawei.android.util.IMonitorEx;
import com.huawei.nb.utils.reporter.Reporter;

public class DataBaseAccessAudit extends Audit {
    private static final int EVENT_ID = 942010005;
    private static final short PARAM_ID_DATABASE = 1;
    private static final short PARAM_ID_DETAIL = 2;
    private static final short PARAM_ID_INTERVAL = 0;
    private final String database;
    private final String detail;
    private final int interval;

    private DataBaseAccessAudit(String database2, String detail2, int interval2) {
        super(EVENT_ID);
        this.database = database2;
        this.detail = detail2;
        this.interval = interval2;
    }

    public IMonitorEx.EventStreamEx createEventStream() {
        IMonitorEx.EventStreamEx eventStreamEx = IMonitorEx.openEventStream(EVENT_ID);
        if (eventStreamEx != null) {
            eventStreamEx.setParam(eventStreamEx, 0, this.interval);
            eventStreamEx.setParam(eventStreamEx, PARAM_ID_DATABASE, this.database);
            eventStreamEx.setParam(eventStreamEx, PARAM_ID_DETAIL, this.detail);
        }
        return eventStreamEx;
    }

    public static void report(String database2, String detail2, int interval2) {
        Reporter.a(new DataBaseAccessAudit(database2, detail2, interval2));
    }
}
