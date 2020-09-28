package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwFullNetworkInitStateHisi2_0 extends HwFullNetworkInitStateHisiBase {
    private static final String LOG_TAG = "HwFullNetworkInitStateHisi2_0";

    public HwFullNetworkInitStateHisi2_0(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkInitStateHisi2_0 constructor");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateHisiBase, com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase
    public void logd(String msg) {
        RlogEx.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateHisiBase, com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
