package com.android.internal.telephony.uicc;

import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;

public class IccIoResult {
    public byte[] payload;
    public int sw1;
    public int sw2;

    public IccIoResult(int sw1, int sw2, byte[] payload) {
        this.sw1 = sw1;
        this.sw2 = sw2;
        this.payload = payload;
    }

    public IccIoResult(int sw1, int sw2, String hexString) {
        this(sw1, sw2, IccUtils.hexStringToBytes(hexString));
    }

    public String toString() {
        return "IccIoResponse sw1:0x" + Integer.toHexString(this.sw1) + " sw2:0x" + Integer.toHexString(this.sw2);
    }

    public boolean success() {
        return this.sw1 == PduPart.P_SECURE || this.sw1 == PduPart.P_SEC || this.sw1 == PduHeaders.REPLY_CHARGING_ID || this.sw1 == PduHeaders.REPLY_CHARGING_SIZE;
    }

    public IccException getException() {
        if (success()) {
            return null;
        }
        switch (this.sw1) {
            case PduPart.P_MODIFICATION_DATE /*148*/:
                if (this.sw2 == 8) {
                    return new IccFileTypeMismatch();
                }
                return new IccFileNotFound();
            default:
                return new IccException("sw1:" + this.sw1 + " sw2:" + this.sw2);
        }
    }
}
