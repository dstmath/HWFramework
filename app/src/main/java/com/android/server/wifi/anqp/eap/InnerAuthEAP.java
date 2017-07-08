package com.android.server.wifi.anqp.eap;

import com.android.server.wifi.anqp.Constants;
import com.android.server.wifi.anqp.eap.EAP.AuthInfoID;
import com.android.server.wifi.anqp.eap.EAP.EAPMethodID;
import java.net.ProtocolException;
import java.nio.ByteBuffer;

public class InnerAuthEAP implements AuthParam {
    private final EAPMethodID mEapMethodID;

    public InnerAuthEAP(int length, ByteBuffer payload) throws ProtocolException {
        if (length != 1) {
            throw new ProtocolException("Bad length: " + length);
        }
        this.mEapMethodID = EAP.mapEAPMethod(payload.get() & Constants.BYTE_MASK);
    }

    public InnerAuthEAP(EAPMethodID eapMethodID) {
        this.mEapMethodID = eapMethodID;
    }

    public AuthInfoID getAuthInfoID() {
        return AuthInfoID.InnerAuthEAPMethodType;
    }

    public EAPMethodID getEAPMethodID() {
        return this.mEapMethodID;
    }

    public int hashCode() {
        return this.mEapMethodID != null ? this.mEapMethodID.hashCode() : 0;
    }

    public boolean equals(Object thatObject) {
        boolean z = true;
        if (thatObject == this) {
            return true;
        }
        if (thatObject == null || thatObject.getClass() != InnerAuthEAP.class) {
            return false;
        }
        if (((InnerAuthEAP) thatObject).getEAPMethodID() != getEAPMethodID()) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return "Auth method InnerAuthEAP, inner = " + this.mEapMethodID + '\n';
    }
}
