package com.huawei.android.app;

import android.os.Bundle;
import android.util.ERecoveryEvent;
import android.util.ERecoveryNative;
import android.util.Log;

public class ERecovery {
    public static final String ERECOVERY_ID = "eRecoveryID";
    public static final String FAULT_ID = "faultID";
    public static final String FINGER_PRINT = "fingerPrint";
    public static final String PID = "pid";
    public static final String PROCESS_NAME = "processName";
    public static final String REASON = "reason";
    public static final String RESERVED = "reserved";
    public static final String RESULT = "result";
    public static final String STATE = "state";
    public static final String TAG = "ERecovery";
    public static final String TIME_STAMP = "timeStamp";

    public static long report(Bundle data) {
        if (data == null) {
            try {
                Log.e(TAG, "Null bundle.");
                return -1;
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "Failed to report event to erecovery.");
                return -1;
            }
        } else {
            ERecoveryEvent eventdata = new ERecoveryEvent();
            eventdata.setERecoveryID(data.getLong(ERECOVERY_ID, 0));
            eventdata.setFaultID(data.getLong(FAULT_ID, 0));
            eventdata.setPid(data.getLong(PID, 0));
            eventdata.setProcessName(data.getString(PROCESS_NAME, ""));
            eventdata.setFingerPrint(data.getString(FINGER_PRINT, ""));
            eventdata.setTimeStamp(data.getLong(TIME_STAMP, 0));
            eventdata.setState(data.getLong("state", 0));
            eventdata.setResult(data.getLong(RESULT, 0));
            eventdata.setReason(data.getString(REASON, ""));
            eventdata.setReserved(data.getString(RESERVED, ""));
            return ERecoveryNative.eRecoveryReport(eventdata);
        }
    }
}
