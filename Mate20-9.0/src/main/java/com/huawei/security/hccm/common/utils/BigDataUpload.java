package com.huawei.security.hccm.common.utils;

import android.util.IMonitor;
import android.util.Log;

public class BigDataUpload {
    public static final int EVENT_STATUS_CODE_NULL_EVENT = -1;
    public static final int EVENT_STATUS_CODE_SEND_FAIL = -3;
    public static final int EVENT_STATUS_CODE_SET_PARAM_FAIL = -2;
    public static final int EVENT_STATUS_CODE_SUCCESS = 0;
    private static final String TAG = "BigDataUpload";

    public static int reportToBigData(int eventId, String paramId, int retVal) {
        Log.d(TAG, "hccm reportToBigData");
        IMonitor.EventStream eStream = IMonitor.openEventStream(eventId);
        if (eStream == null) {
            Log.e(TAG, "open event stream failed");
            return -1;
        } else if (eStream.setParam(paramId, retVal) == null) {
            Log.e(TAG, "set big data param failed");
            return -2;
        } else if (!IMonitor.sendEvent(eStream)) {
            Log.e(TAG, "send event failed");
            return -3;
        } else {
            IMonitor.closeEventStream(eStream);
            return 0;
        }
    }
}
