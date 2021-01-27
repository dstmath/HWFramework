package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.uicc.HwVSimIccCardProxy;
import com.android.internal.telephony.uicc.HwVSimUiccController;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.GsmCdmaPhoneEx;
import com.huawei.internal.telephony.PhoneNotifierEx;

public class HwVSimPhone extends GsmCdmaPhoneEx {
    private static final String LOG_TAG = "VSimPhone";
    private static final int SUB_VSIM = 2;
    private HwVSimIccCardProxy mIccCardProxy;
    private HwVSimUiccController mVsimUiccController = HwVSimUiccController.getInstance();

    HwVSimPhone(Context context, CommandsInterfaceEx ci, PhoneNotifierEx notifier) {
        initGsmCdmaPhone(context, ci, notifier, 2, 1);
        this.mIccCardProxy = new HwVSimIccCardProxy(context, ci);
        this.mVsimUiccController.registerForIccChanged(getHandler(), getEventIccChangedHw(), null);
        logd("VSimPhone: constructor: sub = " + this.mPhoneId);
    }

    public void dispose() {
        this.mVsimUiccController.unregisterForIccChanged(getHandler());
    }

    private void logd(String s) {
        RlogEx.d(LOG_TAG, "[VSimPhone] " + s);
    }

    public HwVSimIccCardProxy getIccCard() {
        logd("getIccCard: " + this.mIccCardProxy);
        return this.mIccCardProxy;
    }

    public boolean hasIccCard() {
        HwVSimIccCardProxy hwVSimIccCardProxy = this.mIccCardProxy;
        if (hwVSimIccCardProxy != null) {
            return hwVSimIccCardProxy.hasIccCard();
        }
        return false;
    }

    public void updateDataConnectionTracker() {
        logd("updateDataConnectionTracker");
        getDcTracker().updateForVSim();
        setInternalDataEnabled(true);
    }
}
