package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwFullNetworkInitStateMtk extends HwFullNetworkInitStateQcomMtkBase {
    private static final String LOG_TAG = "HwFullNetworkInitStateMtk";

    public HwFullNetworkInitStateMtk(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        this.mChipOther = HwFullNetworkChipOther.getInstance();
        initParams();
        logd("HwFullNetworkInitStateMtk constructor");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateQcomMtkBase, com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase
    public void logd(String msg) {
        RlogEx.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateQcomMtkBase, com.android.internal.telephony.fullnetwork.HwFullNetworkInitStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
