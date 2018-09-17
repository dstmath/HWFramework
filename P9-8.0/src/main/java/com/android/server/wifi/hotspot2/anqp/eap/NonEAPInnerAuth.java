package com.android.server.wifi.hotspot2.anqp.eap;

import com.android.server.wifi.hotspot2.anqp.Constants;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class NonEAPInnerAuth extends AuthParam {
    public static final int AUTH_TYPE_CHAP = 2;
    private static final Map<String, Integer> AUTH_TYPE_MAP = new HashMap();
    public static final int AUTH_TYPE_MSCHAP = 3;
    public static final int AUTH_TYPE_MSCHAPV2 = 4;
    public static final int AUTH_TYPE_PAP = 1;
    public static final int AUTH_TYPE_UNKNOWN = 0;
    public static final int EXPECTED_LENGTH_VALUE = 1;
    private final int mAuthType;

    static {
        AUTH_TYPE_MAP.put("PAP", Integer.valueOf(1));
        AUTH_TYPE_MAP.put("CHAP", Integer.valueOf(2));
        AUTH_TYPE_MAP.put("MS-CHAP", Integer.valueOf(3));
        AUTH_TYPE_MAP.put("MS-CHAP-V2", Integer.valueOf(4));
    }

    public NonEAPInnerAuth(int authType) {
        super(2);
        this.mAuthType = authType;
    }

    public static NonEAPInnerAuth parse(ByteBuffer payload, int length) throws ProtocolException {
        if (length == 1) {
            return new NonEAPInnerAuth(payload.get() & Constants.BYTE_MASK);
        }
        throw new ProtocolException("Invalid length: " + length);
    }

    public static int getAuthTypeID(String typeStr) {
        if (AUTH_TYPE_MAP.containsKey(typeStr)) {
            return ((Integer) AUTH_TYPE_MAP.get(typeStr)).intValue();
        }
        return 0;
    }

    public boolean equals(Object thatObject) {
        boolean z = true;
        if (thatObject == this) {
            return true;
        }
        if (!(thatObject instanceof NonEAPInnerAuth)) {
            return false;
        }
        if (this.mAuthType != ((NonEAPInnerAuth) thatObject).mAuthType) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.mAuthType;
    }

    public String toString() {
        return "NonEAPInnerAuth{mAuthType=" + this.mAuthType + "}";
    }
}
