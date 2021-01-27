package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwFullNetworkDefaultStateMtk extends HwFullNetworkDefaultStateQcomMtkBase {
    private static final String LOG_TAG = "HwFullNetworkDefaultStateMtk";

    public HwFullNetworkDefaultStateMtk(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        logd("HwFullNetworkDefaultStateMtk constructor");
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
