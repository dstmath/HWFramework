package com.android.server.wifi.hotspot2.anqp;

import com.android.server.wifi.ByteBufferReader;
import com.android.server.wifi.hotspot2.anqp.eap.EAPMethod;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NAIRealmData {
    public static final int NAI_ENCODING_UTF8_MASK = 1;
    public static final String NAI_REALM_STRING_SEPARATOR = ";";
    private final List<EAPMethod> mEAPMethods;
    private final List<String> mRealms;

    public NAIRealmData(List<String> realms, List<EAPMethod> eapMethods) {
        this.mRealms = realms;
        this.mEAPMethods = eapMethods;
    }

    public static NAIRealmData parse(ByteBuffer payload) throws ProtocolException {
        int length = ((int) ByteBufferReader.readInteger(payload, ByteOrder.LITTLE_ENDIAN, 2)) & Constants.SHORT_MASK;
        if (length > payload.remaining()) {
            throw new ProtocolException("Invalid data length: " + length);
        }
        List<String> realmList = Arrays.asList(ByteBufferReader.readStringWithByteLength(payload, (payload.get() & 1) != 0 ? StandardCharsets.UTF_8 : StandardCharsets.US_ASCII).split(NAI_REALM_STRING_SEPARATOR));
        List<EAPMethod> eapMethodList = new ArrayList();
        for (int methodCount = payload.get() & Constants.BYTE_MASK; methodCount > 0; methodCount--) {
            eapMethodList.add(EAPMethod.parse(payload));
        }
        return new NAIRealmData(realmList, eapMethodList);
    }

    public List<String> getRealms() {
        return Collections.unmodifiableList(this.mRealms);
    }

    public List<EAPMethod> getEAPMethods() {
        return Collections.unmodifiableList(this.mEAPMethods);
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof NAIRealmData)) {
            return false;
        }
        NAIRealmData that = (NAIRealmData) thatObject;
        if (this.mRealms.equals(that.mRealms)) {
            z = this.mEAPMethods.equals(that.mEAPMethods);
        }
        return z;
    }

    public int hashCode() {
        return (this.mRealms.hashCode() * 31) + this.mEAPMethods.hashCode();
    }

    public String toString() {
        return "NAIRealmElement{mRealms=" + this.mRealms + " mEAPMethods=" + this.mEAPMethods + "}";
    }
}
