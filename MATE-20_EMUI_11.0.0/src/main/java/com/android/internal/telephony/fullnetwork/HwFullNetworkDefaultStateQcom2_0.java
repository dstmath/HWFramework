package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwFullNetworkDefaultStateQcom2_0 extends HwFullNetworkDefaultStateQcomMtkBase {
    private static final String LOG_TAG = "HwFullNetworkDefaultStateQcom2_0";

    public HwFullNetworkDefaultStateQcom2_0(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        logd("HwFullNetworkDefaultStateQcom2_0 constructor");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateQcomMtkBase, com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateQcomMtkBase, com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
