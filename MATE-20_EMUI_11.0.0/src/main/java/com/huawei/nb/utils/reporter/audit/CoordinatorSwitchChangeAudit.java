package com.huawei.nb.utils.reporter.audit;

import com.huawei.android.util.IMonitorEx;
import com.huawei.nb.utils.reporter.Reporter;

public class CoordinatorSwitchChangeAudit extends Audit {
    private static final int EVENT_ID_SWITCHCHANGE_EVENT = 942010104;
    private static final short SWITCHCHANGE_EVENT_STATE_VARCHAR = 0;
    private static final short SWITCHCHANGE_EVENT_TRIGGER_VARCHAR = 3;
    private static final short SWITCHCHANGE_EVENT_URL_VARCHAR = 1;
    private static final short SWITCHCHANGE_EVENT_VERSION_VARCHAR = 2;
    private String stateInfo = null;
    private String triggerInfo = null;
    private String urlInfo = null;
    private String versionInfo = null;

    public CoordinatorSwitchChangeAudit(String str, String str2, String str3, String str4) {
        super(EVENT_ID_SWITCHCHANGE_EVENT);
        this.stateInfo = str;
        this.urlInfo = str2;
        this.versionInfo = str3;
        this.triggerInfo = str4;
    }

    @Override // com.huawei.nb.utils.reporter.audit.Audit
    public IMonitorEx.EventStreamEx createEventStream() {
        IMonitorEx.EventStreamEx openEventStream = IMonitorEx.openEventStream((int) EVENT_ID_SWITCHCHANGE_EVENT);
        if (openEventStream != null) {
            openEventStream.setParam(openEventStream, (short) SWITCHCHANGE_EVENT_STATE_VARCHAR, this.stateInfo);
            openEventStream.setParam(openEventStream, (short) SWITCHCHANGE_EVENT_URL_VARCHAR, this.urlInfo);
            openEventStream.setParam(openEventStream, (short) SWITCHCHANGE_EVENT_VERSION_VARCHAR, this.versionInfo);
            openEventStream.setParam(openEventStream, (short) SWITCHCHANGE_EVENT_TRIGGER_VARCHAR, this.triggerInfo);
        }
        return openEventStream;
    }

    public static void report(String str, String str2, String str3, String str4) {
        Reporter.a(new CoordinatorSwitchChangeAudit(str, str2, str3, str4));
    }
}
