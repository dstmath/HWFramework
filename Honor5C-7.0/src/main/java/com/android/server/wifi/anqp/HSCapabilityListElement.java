package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class HSCapabilityListElement extends ANQPElement {
    private final ANQPElementType[] mCapabilities;

    public HSCapabilityListElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        this.mCapabilities = new ANQPElementType[payload.remaining()];
        int index = 0;
        while (payload.hasRemaining()) {
            int capID = payload.get() & Constants.BYTE_MASK;
            ANQPElementType capability = Constants.mapHS20Element(capID);
            if (capability == null) {
                throw new ProtocolException("Unknown capability: " + capID);
            }
            int index2 = index + 1;
            this.mCapabilities[index] = capability;
            index = index2;
        }
    }

    public ANQPElementType[] getCapabilities() {
        return this.mCapabilities;
    }

    public String toString() {
        return "HSCapabilityList{mCapabilities=" + Arrays.toString(this.mCapabilities) + '}';
    }
}
