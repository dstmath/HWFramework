package android.database;

import com.huawei.android.util.IMonitorExt;

public final class DatabaseCorruptAudit extends Audit {
    public static final int EVENT_ID = 942010005;
    private static final short PARAM_ID_DATABASE = 1;
    private static final short PARAM_ID_DETAIL = 2;
    private static final short PARAM_ID_INTERVAL = 0;
    private final String databaseName;

    private DatabaseCorruptAudit(String databaseName2) {
        super(EVENT_ID);
        this.databaseName = databaseName2;
    }

    @Override // android.database.Audit
    public IMonitorExt.EventStreamExt createEventStream() {
        IMonitorExt.EventStreamExt eventStreamExt = IMonitorExt.openEventStream((int) EVENT_ID);
        if (eventStreamExt != null) {
            eventStreamExt.setParam(eventStreamExt, 0, 0);
            eventStreamExt.setParam(eventStreamExt, 1, this.databaseName);
            eventStreamExt.setParam(eventStreamExt, 2, "database is corrupted");
        }
        return eventStreamExt;
    }

    public static void report(String fileName) {
        Reporter.audit(new DatabaseCorruptAudit(fileName));
    }
}
