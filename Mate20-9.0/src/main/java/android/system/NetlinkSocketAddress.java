package android.system;

import java.net.SocketAddress;
import libcore.util.Objects;

public final class NetlinkSocketAddress extends SocketAddress {
    private final int nlGroupsMask;
    private final int nlPortId;

    public NetlinkSocketAddress() {
        this(0, 0);
    }

    public NetlinkSocketAddress(int nlPortId2) {
        this(nlPortId2, 0);
    }

    public NetlinkSocketAddress(int nlPortId2, int nlGroupsMask2) {
        this.nlPortId = nlPortId2;
        this.nlGroupsMask = nlGroupsMask2;
    }

    public int getPortId() {
        return this.nlPortId;
    }

    public int getGroupsMask() {
        return this.nlGroupsMask;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
