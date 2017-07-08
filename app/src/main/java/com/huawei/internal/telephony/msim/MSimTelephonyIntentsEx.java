package com.huawei.internal.telephony.msim;

import com.huawei.android.util.NoExtAPIException;
import com.huawei.internal.telephony.msim.MSimTelephonyManagerEx.MultiSimVariants;

public class MSimTelephonyIntentsEx {
    public static final String ACTION_DEFAULT_SUBSCRIPTION_CHANGED = "qualcomm.intent.action.ACTION_DEFAULT_SUBSCRIPTION_CHANGED";

    public MultiSimVariants getMultiSimConfiguration() {
        throw new NoExtAPIException("method not supported.");
    }
}
