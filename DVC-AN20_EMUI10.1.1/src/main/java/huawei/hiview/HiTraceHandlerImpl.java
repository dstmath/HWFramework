package huawei.hiview;

import android.os.Message;

public class HiTraceHandlerImpl implements HiTraceHandler {
    private static HiTraceHandler instance = new HiTraceHandlerImpl();

    private HiTraceHandlerImpl() {
    }

    public static HiTraceHandler getInstance() {
        return instance;
    }

    public void csTraceInHandler(Message msg) {
        if (msg != null) {
            HiTrace hitrace = HiTraceImpl.getInstance();
            if (hitrace.getId().isValid()) {
                HiTraceId childId = hitrace.createSpan();
                if (childId.isValid()) {
                    msg.traceId = childId;
                    if (childId.isFlagEnabled(4)) {
                        hitrace.tracePoint(0, childId, "(CS)msg send what=%d, arg1=%d, arg2=%d, sendUid=%d, workSrcUid=%d, when=%d", new Object[]{Integer.valueOf(msg.what), Integer.valueOf(msg.arg1), Integer.valueOf(msg.arg2), Integer.valueOf(msg.sendingUid), Integer.valueOf(msg.workSourceUid), Long.valueOf(msg.when)});
                    }
                }
            }
        }
    }

    public void srTraceInLooper(Message msg) {
        if (msg != null) {
            HiTrace hitrace = HiTraceImpl.getInstance();
            HiTraceId traceId = msg.traceId;
            if (traceId == null || !traceId.isValid()) {
                hitrace.clearId();
                return;
            }
            hitrace.setId(traceId);
            if (traceId.isFlagEnabled(4)) {
                hitrace.tracePoint(3, traceId, "(SR) begin handle msg what=%d, arg1=%d, arg2=%d, sendUid=%d, workSrcUid=%d, when=%d", new Object[]{Integer.valueOf(msg.what), Integer.valueOf(msg.arg1), Integer.valueOf(msg.arg2), Integer.valueOf(msg.sendingUid), Integer.valueOf(msg.workSourceUid), Long.valueOf(msg.when)});
            }
        }
    }

    public void ssTraceInLooper(Message msg) {
        if (msg != null) {
            HiTrace hitrace = HiTraceImpl.getInstance();
            HiTraceId traceId = msg.traceId;
            if (traceId != null && traceId.isValid()) {
                if (traceId.isFlagEnabled(4)) {
                    hitrace.tracePoint(2, traceId, "(SS)finish handle msg what=%d, arg1=%d, arg2=%d, sendUid=%d, workSrcUid=%d, when=%d", new Object[]{Integer.valueOf(msg.what), Integer.valueOf(msg.arg1), Integer.valueOf(msg.arg2), Integer.valueOf(msg.sendingUid), Integer.valueOf(msg.workSourceUid), Long.valueOf(msg.when)});
                }
                hitrace.clearId();
            }
        }
    }
}
