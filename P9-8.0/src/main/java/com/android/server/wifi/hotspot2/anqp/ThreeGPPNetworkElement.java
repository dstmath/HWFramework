package com.android.server.wifi.hotspot2.anqp;

import com.android.server.wifi.hotspot2.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThreeGPPNetworkElement extends ANQPElement {
    public static final int GUD_VERSION_1 = 0;
    private final List<CellularNetwork> mNetworks;

    public ThreeGPPNetworkElement(List<CellularNetwork> networks) {
        super(ANQPElementType.ANQP3GPPNetwork);
        this.mNetworks = networks;
    }

    public static ThreeGPPNetworkElement parse(ByteBuffer payload) throws ProtocolException {
        int gudVersion = payload.get() & Constants.BYTE_MASK;
        if (gudVersion != 0) {
            throw new ProtocolException("Unsupported GUD version: " + gudVersion);
        }
        int length = payload.get() & Constants.BYTE_MASK;
        if (length != payload.remaining()) {
            throw new ProtocolException("Mismatch length and buffer size: length=" + length + " bufferSize=" + payload.remaining());
        }
        List<CellularNetwork> networks = new ArrayList();
        while (payload.hasRemaining()) {
            CellularNetwork network = CellularNetwork.parse(payload);
            if (network != null) {
                networks.add(network);
            }
        }
        return new ThreeGPPNetworkElement(networks);
    }

    public List<CellularNetwork> getNetworks() {
        return Collections.unmodifiableList(this.mNetworks);
    }

    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof ThreeGPPNetworkElement)) {
            return false;
        }
        return this.mNetworks.equals(((ThreeGPPNetworkElement) thatObject).mNetworks);
    }

    public int hashCode() {
        return this.mNetworks.hashCode();
    }

    public String toString() {
        return "ThreeGPPNetwork{mNetworks=" + this.mNetworks + "}";
    }
}
