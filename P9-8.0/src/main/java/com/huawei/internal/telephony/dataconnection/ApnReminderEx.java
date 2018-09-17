package com.huawei.internal.telephony.dataconnection;

import android.content.Context;
import com.android.internal.telephony.dataconnection.ApnReminder;

public class ApnReminderEx {
    private static final ApnReminderEx sInstance = new ApnReminderEx();

    public static synchronized ApnReminderEx getInstance() {
        ApnReminderEx apnReminderEx;
        synchronized (ApnReminderEx.class) {
            apnReminderEx = sInstance;
        }
        return apnReminderEx;
    }

    public void restoreApn(Context context, String plmn, String imsi) {
        ApnReminder.getInstance(context).restoreApn(plmn, imsi);
    }

    public void restoreApn(Context context, String plmn, String imsi, int subId) {
        ApnReminder.getInstance(context, subId).restoreApn(plmn, imsi);
    }
}
