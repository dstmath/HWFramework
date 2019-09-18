package com.huawei.nb.utils.reporter.audit;

import com.huawei.android.util.IMonitorEx;
import com.huawei.nb.utils.reporter.Reporter;
import java.util.ArrayList;
import java.util.List;

public class CoordinatorNetworkErrorAudit extends Audit {
    private static final int EVENT_ID_INTERACTION_EVENT = 942010107;
    List<String> fieldList = new ArrayList();

    private CoordinatorNetworkErrorAudit(String errorCode, String errorMessage, String packageName, String url, String network, String date) {
        super(EVENT_ID_INTERACTION_EVENT);
        this.fieldList.add(errorCode);
        this.fieldList.add(errorMessage);
        this.fieldList.add(packageName);
        this.fieldList.add(url);
        this.fieldList.add(network);
        this.fieldList.add(date);
    }

    public IMonitorEx.EventStreamEx createEventStream() {
        IMonitorEx.EventStreamEx eventStreamEx = IMonitorEx.openEventStream(EVENT_ID_INTERACTION_EVENT);
        short fieldListSize = (short) this.fieldList.size();
        if (eventStreamEx != null) {
            for (short i = 0; i < fieldListSize; i = (short) (i + 1)) {
                eventStreamEx.setParam(eventStreamEx, i, this.fieldList.get(i));
            }
        }
        return eventStreamEx;
    }

    public static void report(String errorCode, String errorMessage, String packageName, String url, String network, String date) {
        Reporter.auditWithoutDuplicate(new CoordinatorNetworkErrorAudit(errorCode, errorMessage, packageName, url, network, date));
    }
}
