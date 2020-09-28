package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import com.android.internal.telephony.GsmCdmaPhone;

public class HwCustGsmMmiCode {
    protected GsmCdmaPhone mPhone;

    public HwCustGsmMmiCode(GsmCdmaPhone phone) {
        this.mPhone = phone;
    }

    public CharSequence getErrorMessageEx(AsyncResult ar, CharSequence result) {
        return result;
    }
}
