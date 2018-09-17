package com.android.server.wifi.hotspot2.anqp.eap;

import com.android.server.wifi.hotspot2.anqp.Constants;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EAPMethod {
    private final Map<Integer, Set<AuthParam>> mAuthParams;
    private final int mEAPMethodID;

    public EAPMethod(int methodID, Map<Integer, Set<AuthParam>> authParams) {
        this.mEAPMethodID = methodID;
        this.mAuthParams = authParams;
    }

    public static EAPMethod parse(ByteBuffer payload) throws ProtocolException {
        int length = payload.get() & Constants.BYTE_MASK;
        if (length > payload.remaining()) {
            throw new ProtocolException("Invalid data length: " + length);
        }
        int methodID = payload.get() & Constants.BYTE_MASK;
        Map<Integer, Set<AuthParam>> authParams = new HashMap();
        for (int authCount = payload.get() & Constants.BYTE_MASK; authCount > 0; authCount--) {
            addAuthParam(authParams, parseAuthParam(payload));
        }
        return new EAPMethod(methodID, authParams);
    }

    private static AuthParam parseAuthParam(ByteBuffer payload) throws ProtocolException {
        int authID = payload.get() & Constants.BYTE_MASK;
        int length = payload.get() & Constants.BYTE_MASK;
        switch (authID) {
            case 1:
                return ExpandedEAPMethod.parse(payload, length, false);
            case 2:
                return NonEAPInnerAuth.parse(payload, length);
            case 3:
                return InnerAuthEAP.parse(payload, length);
            case 4:
                return ExpandedEAPMethod.parse(payload, length, true);
            case 5:
                return CredentialType.parse(payload, length, false);
            case 6:
                return CredentialType.parse(payload, length, true);
            case AuthParam.PARAM_TYPE_VENDOR_SPECIFIC /*221*/:
                return VendorSpecificAuth.parse(payload, length);
            default:
                throw new ProtocolException("Unknow Auth Type ID: " + authID);
        }
    }

    private static void addAuthParam(Map<Integer, Set<AuthParam>> paramsMap, AuthParam authParam) {
        Set<AuthParam> authParams = (Set) paramsMap.get(Integer.valueOf(authParam.getAuthTypeID()));
        if (authParams == null) {
            authParams = new HashSet();
            paramsMap.put(Integer.valueOf(authParam.getAuthTypeID()), authParams);
        }
        authParams.add(authParam);
    }

    public Map<Integer, Set<AuthParam>> getAuthParams() {
        return Collections.unmodifiableMap(this.mAuthParams);
    }

    public int getEAPMethodID() {
        return this.mEAPMethodID;
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (thatObject == this) {
            return true;
        }
        if (!(thatObject instanceof EAPMethod)) {
            return false;
        }
        EAPMethod that = (EAPMethod) thatObject;
        if (this.mEAPMethodID == that.mEAPMethodID) {
            z = this.mAuthParams.equals(that.mAuthParams);
        }
        return z;
    }

    public int hashCode() {
        return (this.mEAPMethodID * 31) + this.mAuthParams.hashCode();
    }

    public String toString() {
        return "EAPMethod{mEAPMethodID=" + this.mEAPMethodID + " mAuthParams=" + this.mAuthParams + "}";
    }
}
