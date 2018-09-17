package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CapabilityListElement extends ANQPElement {
    private final ANQPElementType[] mCapabilities;

    public CapabilityListElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        if ((payload.remaining() & 1) == 1) {
            throw new ProtocolException("Odd length");
        }
        this.mCapabilities = new ANQPElementType[(payload.remaining() / 2)];
        int index = 0;
        while (payload.hasRemaining()) {
            int capID = payload.getShort() & Constants.SHORT_MASK;
            ANQPElementType capability = Constants.mapANQPElement(capID);
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
        return "CapabilityList{mCapabilities=" + Arrays.toString(this.mCapabilities) + '}';
    }
}
