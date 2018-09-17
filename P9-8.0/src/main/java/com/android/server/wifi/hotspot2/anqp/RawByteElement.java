package com.android.server.wifi.hotspot2.anqp;

import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RawByteElement extends ANQPElement {
    private final byte[] mPayload;

    public RawByteElement(ANQPElementType infoID, byte[] payload) {
        super(infoID);
        this.mPayload = payload;
    }

    public static RawByteElement parse(ANQPElementType infoID, ByteBuffer payload) {
        byte[] rawBytes = new byte[payload.remaining()];
        if (payload.hasRemaining()) {
            payload.get(rawBytes);
        }
        return new RawByteElement(infoID, rawBytes);
    }

    public byte[] getPayload() {
        return this.mPayload;
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof RawByteElement)) {
            return false;
        }
        RawByteElement that = (RawByteElement) thatObject;
        if (getID() == that.getID()) {
            z = Arrays.equals(this.mPayload, that.mPayload);
        }
        return z;
    }

    public int hashCode() {
        return Arrays.hashCode(this.mPayload);
    }
}
