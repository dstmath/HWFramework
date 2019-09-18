package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public class HwFullNetworkInitStateMtk extends HwFullNetworkInitStateQcomMtkBase {
    private static final String LOG_TAG = "HwFullNetworkInitStateMtk";

    public HwFullNetworkInitStateMtk(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        initParams();
        logd("HwFullNetworkInitStateMtk constructor");
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
