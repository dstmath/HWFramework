package com.android.server.wifi.hotspot2.anqp;

import com.android.server.wifi.ByteBufferReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ProtocolPortTuple {
    public static final int PROTO_STATUS_CLOSED = 0;
    public static final int PROTO_STATUS_OPEN = 1;
    public static final int PROTO_STATUS_UNKNOWN = 2;
    public static final int RAW_BYTE_SIZE = 4;
    private final int mPort;
    private final int mProtocol;
    private final int mStatus;

    public ProtocolPortTuple(int protocol, int port, int status) {
        this.mProtocol = protocol;
        this.mPort = port;
        this.mStatus = status;
    }

    public static ProtocolPortTuple parse(ByteBuffer payload) {
        return new ProtocolPortTuple(payload.get(), ((int) ByteBufferReader.readInteger(payload, ByteOrder.LITTLE_ENDIAN, 2)) & Constants.SHORT_MASK, payload.get() & Constants.BYTE_MASK);
    }

    public int getProtocol() {
        return this.mProtocol;
    }

    public int getPort() {
        return this.mPort;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public boolean equals(Object thatObject) {
        boolean z = true;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof ProtocolPortTuple)) {
            return false;
        }
        ProtocolPortTuple that = (ProtocolPortTuple) thatObject;
        if (this.mProtocol != that.mProtocol || this.mPort != that.mPort) {
            z = false;
        } else if (this.mStatus != that.mStatus) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (((this.mProtocol * 31) + this.mPort) * 31) + this.mStatus;
    }

    public String toString() {
        return "ProtocolTuple{mProtocol=" + this.mProtocol + ", mPort=" + this.mPort + ", mStatus=" + this.mStatus + '}';
    }
}
