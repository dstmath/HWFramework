package com.huawei.nb.utils.reporter.audit;

import com.huawei.android.util.IMonitorEx;
import com.huawei.nb.utils.reporter.Reporter;
import java.util.ArrayList;
import java.util.List;

public class CoordinatorCloudErrorAudit extends Audit {
    private static final int EVENT_ID_INTERACTION_EVENT = 942010106;
    List<String> fieldList = new ArrayList();

    private CoordinatorCloudErrorAudit(String statusCode, String httpMessage, String businessCode, String businessMessage, String packageName, String url, String network, String date) {
        super(EVENT_ID_INTERACTION_EVENT);
        this.fieldList.add(statusCode);
        this.fieldList.add(httpMessage);
        this.fieldList.add(businessCode);
        this.fieldList.add(businessMessage);
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

    public static void report(String statusCode, String httpMessage, String businessCode, String businessMessage, String packageName, String url, String network, String date) {
        Reporter.auditWithoutDuplicate(new CoordinatorCloudErrorAudit(statusCode, httpMessage, businessCode, businessMessage, packageName, url, network, date));
    }
}
