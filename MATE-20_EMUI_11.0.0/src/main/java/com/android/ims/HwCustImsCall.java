package com.android.ims;

import android.content.Context;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;

public class HwCustImsCall {
    public HwCustImsCall(Context context, ImsCallProfile profile) {
    }

    public boolean isCustImsReasonInfo(ImsReasonInfo reasonInfo) {
        return false;
    }

    public ImsReasonInfo getImsReasonInfoByCustReason(ImsReasonInfo reasonInfo) {
        return reasonInfo;
    }
}
