package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public class HwFullNetworkCheckStateQcom2_0 extends HwFullNetworkCheckStateQcomMtkBase {
    private static final String LOG_TAG = "HwFullNetworkCheckStateQcom2_0";

    public HwFullNetworkCheckStateQcom2_0(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkCheckStateQcom2_0 constructor");
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
