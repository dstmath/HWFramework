package com.huawei.android.mms.pdu;

import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.ReadOrigInd;

public class ReadOrigIndEx {
    private ReadOrigIndEx() {
    }

    public static EncodedStringValue[] getTo(ReadOrigInd readOrigInd) {
        if (readOrigInd == null) {
            return null;
        }
        return readOrigInd.getTo();
    }
}
