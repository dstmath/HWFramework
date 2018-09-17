package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import com.android.server.wifi.hotspot2.Utils;
import java.nio.ByteBuffer;

public class GenericBlobElement extends ANQPElement {
    private final byte[] mData;

    public GenericBlobElement(ANQPElementType infoID, ByteBuffer payload) {
        super(infoID);
        this.mData = new byte[payload.remaining()];
        payload.get(this.mData);
    }

    public byte[] getData() {
        return this.mData;
    }

    public String toString() {
        return "Element ID " + getID() + ": " + Utils.toHexString(this.mData);
    }
}
