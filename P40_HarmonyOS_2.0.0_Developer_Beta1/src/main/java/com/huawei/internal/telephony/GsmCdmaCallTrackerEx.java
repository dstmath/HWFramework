package com.huawei.internal.telephony;

import com.android.internal.telephony.GsmCdmaCallTracker;
import com.android.internal.telephony.IHwGsmCdmaCallTrackerEx;

public class GsmCdmaCallTrackerEx {
    GsmCdmaCallTracker mGsmCdmaCallTracker;

    public static GsmCdmaCallTrackerEx getGsmCdmaCallTrackerEx(Object gsmCdmaCallTracker) {
        GsmCdmaCallTrackerEx gsmCdmaCallTrackerEx = new GsmCdmaCallTrackerEx();
        if (gsmCdmaCallTracker instanceof GsmCdmaCallTracker) {
            gsmCdmaCallTrackerEx.setGsmCdmaCallTracker((GsmCdmaCallTracker) gsmCdmaCallTracker);
        }
        return gsmCdmaCallTrackerEx;
    }

    public GsmCdmaCallTracker getGsmCdmaCallTracker() {
        return this.mGsmCdmaCallTracker;
    }

    public void setGsmCdmaCallTracker(GsmCdmaCallTracker gsmCdmaCallTracker) {
        this.mGsmCdmaCallTracker = gsmCdmaCallTracker;
    }

    public IHwGsmCdmaCallTrackerEx getHwGsmCdmaCallTrackerEx() {
        GsmCdmaCallTracker gsmCdmaCallTracker = this.mGsmCdmaCallTracker;
        if (gsmCdmaCallTracker != null) {
            return gsmCdmaCallTracker.getHwGsmCdmaCallTrackerEx();
        }
        return null;
    }

    public void setConnEncryptCallByNumber(String number, boolean val) {
        GsmCdmaCallTracker gsmCdmaCallTracker = this.mGsmCdmaCallTracker;
        if (gsmCdmaCallTracker != null) {
            gsmCdmaCallTracker.getHwGsmCdmaCallTrackerEx().setConnEncryptCallByNumber(number, val);
        }
    }
}
