package ohos.app.dispatcher;

import ohos.app.dispatcher.task.Task;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;

public class HiTraceHelper {
    private static final String ASYNC_TASK_STRING = "async";
    private static final String SYNC_TASK_STRING = "sync";

    public static boolean isValid(HiTraceId hiTraceId) {
        if (hiTraceId == null) {
            return false;
        }
        return hiTraceId.isValid();
    }

    public static void tracePointBeforePost(HiTraceId hiTraceId, Task task, boolean z, String str) {
        String str2 = z ? ASYNC_TASK_STRING : SYNC_TASK_STRING;
        if (task == null) {
            AppLog.d("tracePointBeforePost task is null", new Object[0]);
        } else {
            HiTrace.tracePoint(0, hiTraceId, "%s before post %s task %d", new Object[]{str, str2, Long.valueOf(task.getSequence())});
        }
    }

    public static void tracePointAfterPost(HiTraceId hiTraceId, Task task, boolean z, String str) {
        String str2 = z ? ASYNC_TASK_STRING : SYNC_TASK_STRING;
        if (task == null) {
            AppLog.d("tracePointAfterPost task is null", new Object[0]);
        } else {
            HiTrace.tracePoint(1, hiTraceId, "%s after post %s task %d", new Object[]{str, str2, Long.valueOf(task.getSequence())});
        }
    }

    public static void tracePointBeforeRunning(HiTraceId hiTraceId, Task task) {
        if (task == null) {
            AppLog.d("tracePointBeforeRunning task is null", new Object[0]);
        } else {
            HiTrace.tracePoint(3, hiTraceId, "before running task %d", new Object[]{Long.valueOf(task.getSequence())});
        }
    }

    public static void tracePointAfterRunning(HiTraceId hiTraceId, Task task) {
        if (task == null) {
            AppLog.d("tracePointAfterRunning task is null", new Object[0]);
        } else {
            HiTrace.tracePoint(2, hiTraceId, "after running task %d", new Object[]{Long.valueOf(task.getSequence())});
        }
    }
}
