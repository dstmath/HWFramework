package com.android.internal.telephony;

public class DefaultHwHotplugController {
    private static DefaultHwHotplugController sInstance = new DefaultHwHotplugController();

    public static DefaultHwHotplugController getInstance() {
        return sInstance;
    }

    public void updateHotPlugMainSlotIccId(String iccid) {
    }

    public void onHotplugIccIdChanged(String iccid, int slotId) {
    }
}
