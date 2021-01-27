package com.huawei.nb.utils.reporter.audit;

import com.huawei.android.util.IMonitorEx;
import com.huawei.nb.utils.reporter.Reporter;
import java.util.ArrayList;
import java.util.List;

public class CoordinatorNetworkErrorAudit extends Audit {
    private static final int EVENT_ID_INTERACTION_EVENT = 942010107;
    private List<String> fieldList = new ArrayList();

    public CoordinatorNetworkErrorAudit() {
        super(EVENT_ID_INTERACTION_EVENT);
    }

    @Override // com.huawei.nb.utils.reporter.audit.Audit
    public IMonitorEx.EventStreamEx createEventStream() {
        IMonitorEx.EventStreamEx openEventStream = IMonitorEx.openEventStream((int) EVENT_ID_INTERACTION_EVENT);
        short size = (short) this.fieldList.size();
        if (openEventStream != null) {
            for (short s = 0; s < size; s = (short) (s + 1)) {
                openEventStream.setParam(openEventStream, s, this.fieldList.get(s));
            }
        }
        return openEventStream;
    }

    public static void report(CoordinatorNetworkErrorAudit coordinatorNetworkErrorAudit) {
        Reporter.auditWithoutDuplicate(coordinatorNetworkErrorAudit);
    }

    public void addStatusCodeToList(String str) {
        this.fieldList.add(str);
    }

    public void addResponseMessageToList(String str) {
        this.fieldList.add(str);
    }

    public void addPackageNameToList(String str) {
        this.fieldList.add(str);
    }

    public void addUrlToList(String str) {
        this.fieldList.add(str);
    }

    public void addNetworkToList(String str) {
        this.fieldList.add(str);
    }

    public void addDateToList(String str) {
        this.fieldList.add(str);
    }
}
