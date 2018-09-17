package com.android.internal.telephony.jni;

import com.android.internal.telephony.ServiceStateTracker;

public class OperateTA {
    public native int operTA(int i, int i2, int i3, String str);

    static {
        System.loadLibrary("operta");
    }

    public int writeToTA(int cardType, int apnType, String Challenge) {
        operTA(1, cardType, apnType, Challenge);
        return operTA(2, cardType, apnType, Challenge);
    }

    public int writeApnToTA(int cardType, int apnType, String Challenge) {
        return operTA(2, cardType, apnType, Challenge);
    }

    public int clearToTA() {
        return operTA(3, 0, 0, ServiceStateTracker.INVALID_MCC);
    }
}
