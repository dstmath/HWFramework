package com.android.server.wifi.hotspot2.anqp;

import com.android.server.wifi.ByteBufferReader;
import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NAIRealmElement extends ANQPElement {
    private final List<NAIRealmData> mRealmDataList;

    public NAIRealmElement(List<NAIRealmData> realmDataList) {
        super(ANQPElementType.ANQPNAIRealm);
        this.mRealmDataList = realmDataList;
    }

    public static NAIRealmElement parse(ByteBuffer payload) throws ProtocolException {
        List<NAIRealmData> realmDataList = new ArrayList();
        if (payload.hasRemaining()) {
            for (int count = ((int) ByteBufferReader.readInteger(payload, ByteOrder.LITTLE_ENDIAN, 2)) & Constants.SHORT_MASK; count > 0; count--) {
                realmDataList.add(NAIRealmData.parse(payload));
            }
        }
        return new NAIRealmElement(realmDataList);
    }

    public List<NAIRealmData> getRealmDataList() {
        return Collections.unmodifiableList(this.mRealmDataList);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof NAIRealmElement)) {
            return false;
        }
        return this.mRealmDataList.equals(((NAIRealmElement) thatObject).mRealmDataList);
    }

    public int hashCode() {
        return this.mRealmDataList.hashCode();
    }

    public String toString() {
        return "NAIRealmElement{mRealmDataList=" + this.mRealmDataList + "}";
    }
}
