package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwFullNetworkCheckStateMtk extends HwFullNetworkCheckStateQcomMtkBase {
    private static final String LOG_TAG = "HwFullNetworkCheckStateMtk";

    public HwFullNetworkCheckStateMtk(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkCheckStateMtk constructor");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateQcomMtkBase, com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateQcomMtkBase, com.android.internal.telephony.fullnetwork.HwFullNetworkCheckStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
