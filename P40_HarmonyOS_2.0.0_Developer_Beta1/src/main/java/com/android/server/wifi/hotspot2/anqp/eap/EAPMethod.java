package com.android.server.wifi.hotspot2.anqp.eap;

import com.android.internal.annotations.VisibleForTesting;
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

    @VisibleForTesting
    public EAPMethod(int methodID, Map<Integer, Set<AuthParam>> authParams) {
        this.mEAPMethodID = methodID;
        this.mAuthParams = authParams;
    }

    public static EAPMethod parse(ByteBuffer payload) throws ProtocolException {
        int length = payload.get() & 255;
        if (length <= payload.remaining()) {
            int methodID = payload.get() & 255;
            Map<Integer, Set<AuthParam>> authParams = new HashMap<>();
            for (int authCount = payload.get() & 255; authCount > 0; authCount--) {
                addAuthParam(authParams, parseAuthParam(payload));
            }
            return new EAPMethod(methodID, authParams);
        }
        throw new ProtocolException("Invalid data length: " + length);
    }

    private static AuthParam parseAuthParam(ByteBuffer payload) throws ProtocolException {
        int authID = payload.get() & 255;
        int length = payload.get() & 255;
        if (authID == 221) {
            return VendorSpecificAuth.parse(payload, length);
        }
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
            default:
                throw new ProtocolException("Unknow Auth Type ID: " + authID);
        }
    }

    private static void addAuthParam(Map<Integer, Set<AuthParam>> paramsMap, AuthParam authParam) {
        Set<AuthParam> authParams = paramsMap.get(Integer.valueOf(authParam.getAuthTypeID()));
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
        if (thatObject == this) {
            return true;
        }
        if (!(thatObject instanceof EAPMethod)) {
            return false;
        }
        EAPMethod that = (EAPMethod) thatObject;
        if (this.mEAPMethodID != that.mEAPMethodID || !this.mAuthParams.equals(that.mAuthParams)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (this.mEAPMethodID * 31) + this.mAuthParams.hashCode();
    }

    public String toString() {
        return "EAPMethod{mEAPMethodID=" + this.mEAPMethodID + " mAuthParams=" + this.mAuthParams + "}";
    }
}
