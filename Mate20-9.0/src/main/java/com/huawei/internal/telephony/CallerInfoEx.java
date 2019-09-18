package com.huawei.internal.telephony;

import android.content.Context;
import android.net.Uri;
import com.android.internal.telephony.CallerInfo;

public class CallerInfoEx {
    private CallerInfo mCallerInfo;

    public CallerInfoEx(CallerInfo callerInfo) {
        this.mCallerInfo = callerInfo;
    }

    public static CallerInfoEx getCallerInfo(Context context, Uri contactRef) {
        return new CallerInfoEx(CallerInfo.getCallerInfo(context, contactRef));
    }

    public static CallerInfoEx getCallerInfo(Context context, String number) {
        return new CallerInfoEx(CallerInfo.getCallerInfo(context, number));
    }

    public String getPhoneNumber() {
        if (this.mCallerInfo == null) {
            return null;
        }
        return this.mCallerInfo.phoneNumber;
    }
}
