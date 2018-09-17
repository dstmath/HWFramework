package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import com.android.server.wifi.hotspot2.Utils;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoamingConsortiumElement extends ANQPElement {
    private final List<Long> mOis;

    public RoamingConsortiumElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        this.mOis = new ArrayList();
        while (payload.hasRemaining()) {
            int length = payload.get() & Constants.BYTE_MASK;
            if (length > payload.remaining()) {
                throw new ProtocolException("Bad OI length: " + length);
            }
            this.mOis.add(Long.valueOf(Constants.getInteger(payload, ByteOrder.BIG_ENDIAN, length)));
        }
    }

    public List<Long> getOIs() {
        return Collections.unmodifiableList(this.mOis);
    }

    public String toString() {
        return "RoamingConsortium{mOis=[" + Utils.roamingConsortiumsToString(this.mOis) + "]}";
    }
}
