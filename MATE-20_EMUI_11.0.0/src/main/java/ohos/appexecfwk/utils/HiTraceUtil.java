package ohos.appexecfwk.utils;

import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;

public class HiTraceUtil {
    public static boolean isValid(HiTraceId hiTraceId) {
        if (hiTraceId == null) {
            return false;
        }
        return hiTraceId.isValid();
    }

    public static HiTraceId hiTraceBegin(String str) {
        return HiTrace.begin(str, 1);
    }

    public static void tracePointBeforeRemote(String str, String str2) {
        HiTraceId id = HiTrace.getId();
        if (!isValid(id)) {
            AppLog.e("HiTraceUtil::tracePointBeforeRemote failed, hiTraceId is invalid", new Object[0]);
        } else {
            HiTrace.tracePoint(4, id, "before %s abilityName: %s", new Object[]{str, str2});
        }
    }

    public static void tracePointAfterRemote(String str, String str2) {
        HiTraceId id = HiTrace.getId();
        if (!isValid(id)) {
            AppLog.e("HiTraceUtil::tracePointAfterRemote failed, hiTraceId is invalid", new Object[0]);
        } else {
            HiTrace.tracePoint(4, id, "after %s abilityName: %s", new Object[]{str, str2});
        }
    }
}
