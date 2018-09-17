package com.android.internal.telephony;

import android.telephony.Rlog;

public class HwUiccSmsController extends UiccSmsController {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "HwUiccSmsController";
    private static UiccSmsControllerUtils utils = new UiccSmsControllerUtils();

    public HwUiccSmsController(Phone[] phone) {
        super(phone);
    }

    public String getSmscAddr() {
        return getSmscAddrForSubscriber((long) getPreferredSmsSubscription());
    }

    public String getSmscAddrForSubscriber(long subId) {
        IccSmsInterfaceManager iccSmsIntMgr = utils.getIccSmsInterfaceManager(this, (int) subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getSmscAddr();
        }
        Rlog.e(LOG_TAG, "getSmscAddr iccSmsIntMgr is null for Subscription: " + subId);
        return null;
    }

    public boolean setSmscAddr(String smscAddr) {
        return setSmscAddrForSubscriber((long) getPreferredSmsSubscription(), smscAddr);
    }

    public boolean setSmscAddrForSubscriber(long subId, String smscAddr) {
        IccSmsInterfaceManager iccSmsIntMgr = utils.getIccSmsInterfaceManager(this, (int) subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.setSmscAddr(smscAddr);
        }
        Rlog.e(LOG_TAG, "setSmscAddr iccSmsIntMgr is null for Subscription: " + subId);
        return false;
    }
}
