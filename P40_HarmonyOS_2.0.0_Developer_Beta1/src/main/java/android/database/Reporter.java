package android.database;

import com.huawei.android.util.IMonitorExt;

public class Reporter {
    public static void audit(Audit audit) {
        IMonitorExt.EventStreamExt eventStreamExt = audit.createEventStream();
        IMonitorExt.sendEvent(eventStreamExt);
        IMonitorExt.closeEventStream(eventStreamExt);
    }
}
