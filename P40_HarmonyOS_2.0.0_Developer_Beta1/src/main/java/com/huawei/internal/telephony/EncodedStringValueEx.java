package com.huawei.internal.telephony;

import com.google.android.mms.pdu.EncodedStringValue;

public class EncodedStringValueEx {
    private EncodedStringValue mEncodedStringValue;

    public void setEncodedStringValue(EncodedStringValue encodedStringValue) {
        this.mEncodedStringValue = encodedStringValue;
    }

    public String getEncodedString() {
        EncodedStringValue encodedStringValue = this.mEncodedStringValue;
        if (encodedStringValue != null) {
            return encodedStringValue.getString();
        }
        return null;
    }
}
