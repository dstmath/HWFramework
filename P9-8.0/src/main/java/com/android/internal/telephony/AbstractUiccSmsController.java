package com.android.internal.telephony;

import com.android.internal.telephony.ISms.Stub;

public abstract class AbstractUiccSmsController extends Stub {
    public String getSmscAddr() {
        return null;
    }

    public String getSmscAddrForSubscriber(long subId) {
        return null;
    }

    public boolean setSmscAddr(String smscAddr) {
        return false;
    }

    public boolean setSmscAddrForSubscriber(long subId, String smscAddr) {
        return false;
    }

    public void setEnabledSingleShiftTables(int[] tables) {
    }

    public void setSmsCodingNationalCode(String code) {
    }
}
