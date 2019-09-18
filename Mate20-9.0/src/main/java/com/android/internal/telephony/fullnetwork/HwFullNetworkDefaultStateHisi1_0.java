package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public class HwFullNetworkDefaultStateHisi1_0 extends HwFullNetworkDefaultStateHisiBase {
    private static final String LOG_TAG = "HwFullNetworkDefaultStateHisi1_0";

    public HwFullNetworkDefaultStateHisi1_0(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkDefaultStateHisi1_0 constructor");
    }

    /* access modifiers changed from: protected */
    public void onRadioUnavailable(Integer index) {
        super.onRadioUnavailable(index);
        this.mChipHisi.mOldMainSwitchTypes[index.intValue()] = -1;
        boolean[] zArr = this.mChipHisi.mRadioOn;
        int length = zArr.length;
        int i = 0;
        while (i < length) {
            if (zArr[i]) {
                i++;
            } else {
                return;
            }
        }
        if (1 != 0) {
            this.mChipHisi.setCommrilRestartRild(true);
        }
    }

    /* access modifiers changed from: protected */
    public void processSubSetUiccResult(Intent intent) {
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
