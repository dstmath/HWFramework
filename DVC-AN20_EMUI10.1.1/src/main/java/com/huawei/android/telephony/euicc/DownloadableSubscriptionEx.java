package com.huawei.android.telephony.euicc;

import android.telephony.euicc.DownloadableSubscription;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DownloadableSubscriptionEx {
    public static String getCarrierName(DownloadableSubscription downloadableSubscription) {
        if (downloadableSubscription == null) {
            return null;
        }
        return downloadableSubscription.getCarrierName();
    }
}
