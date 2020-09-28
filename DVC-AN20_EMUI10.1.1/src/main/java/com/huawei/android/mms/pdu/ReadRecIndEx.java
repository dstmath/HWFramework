package com.huawei.android.mms.pdu;

import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.ReadRecInd;

public class ReadRecIndEx {
    private ReadRecIndEx() {
    }

    public static EncodedStringValue[] getTo(ReadRecInd readRecInd) {
        if (readRecInd == null) {
            return null;
        }
        return readRecInd.getTo();
    }
}
