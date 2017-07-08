package com.android.internal.telephony;

import com.android.internal.telephony.IPhoneSubInfo.Stub;

public abstract class AbstractPhoneSubInfo extends Stub {
    public static final String PERMISSION_DENIED_IMEI = "000000000000000";

    public String getPesn() {
        return HwTelephonyFactory.getHwPhoneManager().getPesn(this);
    }

    public boolean isReadPhoneNumberBlocked() {
        return false;
    }
}
