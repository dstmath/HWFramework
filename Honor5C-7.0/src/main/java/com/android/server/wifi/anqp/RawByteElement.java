package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.nio.ByteBuffer;

public class RawByteElement extends ANQPElement {
    private final byte[] mPayload;

    public RawByteElement(ANQPElementType infoID, ByteBuffer payload) {
        super(infoID);
        this.mPayload = new byte[payload.remaining()];
        payload.get(this.mPayload);
    }

    public byte[] getPayload() {
        return this.mPayload;
    }
}
