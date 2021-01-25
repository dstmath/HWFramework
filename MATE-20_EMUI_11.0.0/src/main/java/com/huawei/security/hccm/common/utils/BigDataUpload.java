package com.huawei.security.hccm.common.utils;

import android.util.Log;
import com.huawei.android.util.IMonitorExt;

public class BigDataUpload {
    public static final int EVENT_STATUS_CODE_NULL_EVENT = -1;
    public static final int EVENT_STATUS_CODE_SEND_FAIL = -3;
    public static final int EVENT_STATUS_CODE_SET_PARAM_FAIL = -2;
    public static final int EVENT_STATUS_CODE_SUCCESS = 0;
    private static final String TAG = "BigDataUpload";

    public static int reportToBigData(int eventId, String paramId, int retVal) {
        Log.d(TAG, "Hccm reportToBigData");
        IMonitorExt.EventStreamExt eStream = IMonitorExt.openEventStream(eventId);
        if (eStream == null) {
            try {
                Log.e(TAG, "Open event stream failed");
                return -1;
            } finally {
                IMonitorExt.closeEventStream(eStream);
            }
        } else if (eStream.setParam(paramId, retVal) == null) {
            Log.e(TAG, "Set big data param failed");
            IMonitorExt.closeEventStream(eStream);
            return -2;
        } else if (!IMonitorExt.sendEvent(eStream)) {
            Log.e(TAG, "Send event failed");
            IMonitorExt.closeEventStream(eStream);
            return -3;
        } else {
            IMonitorExt.closeEventStream(eStream);
            return 0;
        }
    }
}
