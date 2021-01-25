package com.android.server.wifi.hotspot2.anqp;

import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HSConnectionCapabilityElement extends ANQPElement {
    private final List<ProtocolPortTuple> mStatusList;

    @VisibleForTesting
    public HSConnectionCapabilityElement(List<ProtocolPortTuple> statusList) {
        super(Constants.ANQPElementType.HSConnCapability);
        this.mStatusList = statusList;
    }

    public static HSConnectionCapabilityElement parse(ByteBuffer payload) {
        List<ProtocolPortTuple> statusList = new ArrayList<>();
        while (payload.hasRemaining()) {
            statusList.add(ProtocolPortTuple.parse(payload));
        }
        return new HSConnectionCapabilityElement(statusList);
    }

    public List<ProtocolPortTuple> getStatusList() {
        return Collections.unmodifiableList(this.mStatusList);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof HSConnectionCapabilityElement)) {
            return false;
        }
        return this.mStatusList.equals(((HSConnectionCapabilityElement) thatObject).mStatusList);
    }

    public int hashCode() {
        return this.mStatusList.hashCode();
    }

    public String toString() {
        return "HSConnectionCapability{mStatusList=" + this.mStatusList + '}';
    }
}
