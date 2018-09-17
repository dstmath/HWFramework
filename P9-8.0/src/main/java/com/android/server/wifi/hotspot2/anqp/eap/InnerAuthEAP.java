package com.android.server.wifi.hotspot2.anqp.eap;

import com.android.server.wifi.hotspot2.anqp.Constants;
import java.net.ProtocolException;
import java.nio.ByteBuffer;

public class InnerAuthEAP extends AuthParam {
    public static final int EXPECTED_LENGTH_VALUE = 1;
    private final int mEAPMethodID;

    public InnerAuthEAP(int eapMethodID) {
        super(3);
        this.mEAPMethodID = eapMethodID;
    }

    public static InnerAuthEAP parse(ByteBuffer payload, int length) throws ProtocolException {
        if (length == 1) {
            return new InnerAuthEAP(payload.get() & Constants.BYTE_MASK);
        }
        throw new ProtocolException("Invalid length: " + length);
    }

    public int getEAPMethodID() {
        return this.mEAPMethodID;
    }

    public boolean equals(Object thatObject) {
        boolean z = true;
        if (thatObject == this) {
            return true;
        }
        if (!(thatObject instanceof InnerAuthEAP)) {
            return false;
        }
        if (this.mEAPMethodID != ((InnerAuthEAP) thatObject).mEAPMethodID) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.mEAPMethodID;
    }

    public String toString() {
        return "InnerAuthEAP{mEAPMethodID=" + this.mEAPMethodID + "}";
    }
}
