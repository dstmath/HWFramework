package com.huawei.internal.telephony.gsm;

import com.android.internal.telephony.gsm.GsmSmsAddress;
import com.huawei.internal.telephony.SmsAddressEx;

public class GsmSmsAddressEx extends SmsAddressEx {
    private GsmSmsAddress mGsmSmsAddress;

    public void setGsmSmsAddress(GsmSmsAddress gsmSmsAddress) {
        this.mGsmSmsAddress = gsmSmsAddress;
        this.mSmsAddress = this.mGsmSmsAddress;
    }
}
