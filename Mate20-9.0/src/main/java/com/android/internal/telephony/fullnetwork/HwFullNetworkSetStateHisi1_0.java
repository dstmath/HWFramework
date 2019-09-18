package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public class HwFullNetworkSetStateHisi1_0 extends HwFullNetworkSetStateBase {
    private static final String LOG_TAG = "HwFullNetworkSetStateHisi1_0";

    HwFullNetworkSetStateHisi1_0(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkSetStateHisi1_0 constructor");
    }

    public void setMainSlot(int slotId, Message response) {
    }

    public void setMainSlotDone(Message response, int index) {
    }

    /* access modifiers changed from: protected */
    public void setRadioCapabilityFailed(Intent intent) {
    }

    /* access modifiers changed from: protected */
    public void setRadioCapabilityDone(Intent intent) {
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
