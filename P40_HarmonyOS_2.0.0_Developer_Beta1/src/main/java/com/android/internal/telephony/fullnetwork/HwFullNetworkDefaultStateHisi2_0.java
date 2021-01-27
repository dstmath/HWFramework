package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwFullNetworkDefaultStateHisi2_0 extends HwFullNetworkDefaultStateHisiBase {
    private static final String LOG_TAG = "HwFullNetworkDefaultStateHisi2_0";

    public HwFullNetworkDefaultStateHisi2_0(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkDefaultStateHisi2_0 constructor");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateHisiBase, com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void onRadioUnavailable(Integer index) {
        super.onRadioUnavailable(index);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void processSubSetUiccResult(Intent intent) {
        handleSetUiccSubscriptionDone(intent);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
