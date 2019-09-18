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

    public CoordinatorSwitchChangeAudit(String stateInfo2, String urlInfo2, String versionInfo2, String triggerInfo2) {
        super(EVENT_ID_SWITCHCHANGE_EVENT);
        this.stateInfo = stateInfo2;
        this.urlInfo = urlInfo2;
        this.versionInfo = versionInfo2;
        this.triggerInfo = triggerInfo2;
    }

    public IMonitorEx.EventStreamEx createEventStream() {
        IMonitorEx.EventStreamEx eventStreamEx = IMonitorEx.openEventStream(EVENT_ID_SWITCHCHANGE_EVENT);
        if (eventStreamEx != null) {
            eventStreamEx.setParam(eventStreamEx, SWITCHCHANGE_EVENT_STATE_VARCHAR, this.stateInfo);
            eventStreamEx.setParam(eventStreamEx, SWITCHCHANGE_EVENT_URL_VARCHAR, this.urlInfo);
            eventStreamEx.setParam(eventStreamEx, SWITCHCHANGE_EVENT_VERSION_VARCHAR, this.versionInfo);
            eventStreamEx.setParam(eventStreamEx, SWITCHCHANGE_EVENT_TRIGGER_VARCHAR, this.triggerInfo);
        }
        return eventStreamEx;
    }

    public static void report(String stateInfo2, String urlInfo2, String versionInfo2, String triggerInfo2) {
        Reporter.a(new CoordinatorSwitchChangeAudit(stateInfo2, urlInfo2, versionInfo2, triggerInfo2));
    }
}
