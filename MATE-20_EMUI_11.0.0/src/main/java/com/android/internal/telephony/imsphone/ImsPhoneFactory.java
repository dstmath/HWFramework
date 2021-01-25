package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.telephony.Rlog;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneNotifier;

public class ImsPhoneFactory {
    public static ImsPhone makePhone(Context context, PhoneNotifier phoneNotifier, Phone defaultPhone) {
        try {
            return new ImsPhone(context, phoneNotifier, defaultPhone);
        } catch (Exception e) {
            Rlog.e("VoltePhoneFactory", "makePhone", e);
            return null;
        }
    }
}
