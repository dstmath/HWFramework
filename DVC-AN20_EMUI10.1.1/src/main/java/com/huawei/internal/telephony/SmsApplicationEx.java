package com.huawei.internal.telephony;

import android.content.ComponentName;
import android.content.Context;
import com.android.internal.telephony.SmsApplication;

public class SmsApplicationEx {
    public static boolean isDefaultSmsApplication(Context context, String packageName) {
        return SmsApplication.isDefaultSmsApplication(context, packageName);
    }

    public static ComponentName getDefaultMmsApplication(Context context, boolean updateIfNeeded) {
        return SmsApplication.getDefaultMmsApplication(context, updateIfNeeded);
    }
}
