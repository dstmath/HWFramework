package com.android.internal.telephony.euicc;

import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.euicc.DownloadableSubscription;

public interface IHwEuiccControllerEx {
    void cancelSession();

    void putIccidByDownloadableSubscription(PendingIntent pendingIntent, Intent intent, DownloadableSubscription downloadableSubscription);

    void putSubIdForVsim(PendingIntent pendingIntent, Intent intent);

    void requestDefaultSmdpAddress(String str, PendingIntent pendingIntent);

    void resetMemory(String str, int i, PendingIntent pendingIntent);

    void setDefaultSmdpAddress(String str, String str2, PendingIntent pendingIntent);
}
