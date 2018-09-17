package com.android.server.wifi.hotspot2.anqp.eap;

import com.android.server.wifi.hotspot2.anqp.Constants;
import java.net.ProtocolException;
import java.nio.ByteBuffer;

public class CredentialType extends AuthParam {
    public static final int CREDENTIAL_TYPE_ANONYMOUS = 9;
    public static final int CREDENTIAL_TYPE_CERTIFICATE = 6;
    public static final int CREDENTIAL_TYPE_HARDWARE_TOKEN = 4;
    public static final int CREDENTIAL_TYPE_NFC = 3;
    public static final int CREDENTIAL_TYPE_NONE = 8;
    public static final int CREDENTIAL_TYPE_SIM = 1;
    public static final int CREDENTIAL_TYPE_SOFTWARE_TOKEN = 5;
    public static final int CREDENTIAL_TYPE_USERNAME_PASSWORD = 7;
    public static final int CREDENTIAL_TYPE_USIM = 2;
    public static final int CREDENTIAL_TYPE_VENDOR_SPECIFIC = 10;
    public static final int EXPECTED_LENGTH_VALUE = 1;
    private final int mType;

    public CredentialType(int authType, int credType) {
        super(authType);
        this.mType = credType;
    }

    public static CredentialType parse(ByteBuffer payload, int length, boolean tunneled) throws ProtocolException {
        if (length != 1) {
            throw new ProtocolException("Invalid length: " + length);
        }
        int authType;
        int credType = payload.get() & Constants.BYTE_MASK;
        if (tunneled) {
            authType = 6;
        } else {
            authType = 5;
        }
        return new CredentialType(authType, credType);
    }

    public int getType() {
        return this.mType;
    }

    public boolean equals(Object thatObject) {
        boolean z = true;
        if (thatObject == this) {
            return true;
        }
        if (!(thatObject instanceof CredentialType)) {
            return false;
        }
        if (this.mType != ((CredentialType) thatObject).mType) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.mType;
    }

    public String toString() {
        return "CredentialType{mType=" + this.mType + "}";
    }
}
