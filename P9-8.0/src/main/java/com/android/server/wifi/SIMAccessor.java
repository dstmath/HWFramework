package com.android.server.wifi;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import java.util.ArrayList;
import java.util.List;

public class SIMAccessor {
    private final SubscriptionManager mSubscriptionManager;
    private final TelephonyManager mTelephonyManager;

    public SIMAccessor(Context context) {
        this.mTelephonyManager = TelephonyManager.from(context);
        this.mSubscriptionManager = SubscriptionManager.from(context);
    }

    public List<String> getMatchingImsis(IMSIParameter mccMnc) {
        if (mccMnc == null) {
            return null;
        }
        List<String> imsis = new ArrayList();
        for (int subId : this.mSubscriptionManager.getActiveSubscriptionIdList()) {
            String imsi = this.mTelephonyManager.getSubscriberId(subId);
            if (imsi != null && mccMnc.matchesImsi(imsi)) {
                imsis.add(imsi);
            }
        }
        if (imsis.isEmpty()) {
            imsis = null;
        }
        return imsis;
    }
}
