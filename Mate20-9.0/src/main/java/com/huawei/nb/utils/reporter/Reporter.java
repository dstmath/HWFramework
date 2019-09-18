package com.huawei.nb.utils.reporter;

import com.huawei.android.util.IMonitorEx;
import com.huawei.nb.utils.logger.DSLog;
import com.huawei.nb.utils.reporter.audit.Audit;
import com.huawei.nb.utils.reporter.fault.Fault;
import java.util.HashMap;

public class Reporter {
    private static final String TAG = "Reporter";
    private static final long WRITE_AUDIT_INTERVAL = 86400000;
    private static final long WRITE_LOG_INTERVAL = 43200000;
    private static HashMap<Integer, Long> sAuditMap = new HashMap<>();
    private static HashMap<String, Object> sExceptionLogMap = new HashMap<>();

    public static void f(Fault fault) {
        if (!isDuplicatedLog(fault)) {
            IMonitorEx.EventStreamEx eventStreamEx = fault.createEventStream();
            IMonitorEx.sendEvent(eventStreamEx);
            IMonitorEx.closeEventStream(eventStreamEx);
        }
    }

    public static void a(Audit audit) {
        IMonitorEx.EventStreamEx eventStreamEx = audit.createEventStream();
        IMonitorEx.sendEvent(eventStreamEx);
        IMonitorEx.closeEventStream(eventStreamEx);
    }

    private static boolean isDuplicatedLog(Fault fault) {
        if (fault == null) {
            DSLog.w("Reporter Fail to report audit log, error: fault is empty.", new Object[0]);
            return true;
        }
        long currentTime = System.currentTimeMillis();
        String faultKey = fault.getKeyMessage();
        if (!sExceptionLogMap.containsKey(faultKey) || currentTime - ((Long) sExceptionLogMap.get(faultKey)).longValue() >= WRITE_LOG_INTERVAL) {
            sExceptionLogMap.put(faultKey, Long.valueOf(currentTime));
            return false;
        }
        DSLog.w("Fault log is duplicated, not report this time.", new Object[0]);
        return true;
    }

    public static void auditWithoutDuplicate(Audit audit) {
        if (audit == null) {
            DSLog.w("Reporter Fail to report audit log, error: audit is empty.", new Object[0]);
        } else if (!isDuplicatedAudit(audit)) {
            IMonitorEx.EventStreamEx eventStreamEx = audit.createEventStream();
            IMonitorEx.sendEvent(eventStreamEx);
            IMonitorEx.closeEventStream(eventStreamEx);
        }
    }

    private static boolean isDuplicatedAudit(Audit audit) {
        long currentTime = System.currentTimeMillis();
        int auditId = audit.getAuditId();
        if (!sAuditMap.containsKey(Integer.valueOf(auditId)) || currentTime - sAuditMap.get(Integer.valueOf(auditId)).longValue() >= 86400000) {
            sAuditMap.put(Integer.valueOf(auditId), Long.valueOf(currentTime));
            return false;
        }
        DSLog.w("Audit log is duplicated, not report this time.", new Object[0]);
        return true;
    }
}
