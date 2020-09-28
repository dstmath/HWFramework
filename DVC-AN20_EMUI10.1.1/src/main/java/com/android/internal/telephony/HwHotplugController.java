package com.android.internal.telephony;

import android.util.Log;

public class HwHotplugController {
    public static final boolean IS_HOTSWAP_SUPPORT = HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyFactoryImpl").createHwUiccManager().isHotswapSupported();
    private static final String TAG = "HwHotplugController";
    private static HwHotplugController sInstance;
    private DefaultHwHotplugController mHwHotplugController = HwPartOptTelephonyFactory.getTelephonyFactory().getFullnetworkFactory().getHotplugController();

    private HwHotplugController() {
        Log.d(TAG, "add " + this.mHwHotplugController + " to memory");
    }

    public static HwHotplugController getInstance() {
        if (sInstance == null) {
            sInstance = new HwHotplugController();
        }
        return sInstance;
    }

    public void updateHotPlugMainSlotIccId(String iccid) {
        this.mHwHotplugController.updateHotPlugMainSlotIccId(iccid);
    }

    public void onHotplugIccIdChanged(String iccid, int slotId) {
        this.mHwHotplugController.onHotplugIccIdChanged(iccid, slotId);
    }
}
