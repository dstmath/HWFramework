package com.android.internal.telephony.cdma;

import com.android.internal.telephony.GsmCdmaPhone;

public class CDMAPhoneUtils {
    public String getMeid(GsmCdmaPhone cdmaPhone) {
        return cdmaPhone.getMeidHw();
    }

    public void setMeid(GsmCdmaPhone cdmaPhone, String value) {
        cdmaPhone.setMeidHw(value);
    }
}
