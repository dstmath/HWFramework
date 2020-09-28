package com.android.internal.telephony;

import android.os.Handler;

public class DefaultHwHotplugController extends Handler {
    private static DefaultHwHotplugController sInstance = new DefaultHwHotplugController();

    public static DefaultHwHotplugController getInstance() {
        return sInstance;
    }

    public void updateHotPlugMainSlotIccId(String iccid) {
    }

    public void onHotplugIccIdChanged(String iccid, int slotId) {
    }
}
