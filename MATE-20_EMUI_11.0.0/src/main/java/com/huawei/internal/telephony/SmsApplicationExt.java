package com.huawei.internal.telephony;

import android.content.ComponentName;
import android.content.Context;
import com.android.internal.telephony.SmsApplication;

public class SmsApplicationExt {
    public static ComponentName getDefaultSmsApplication(Context context, boolean updateIfNeeded) {
        return SmsApplication.getDefaultSmsApplication(context, updateIfNeeded);
    }
}
