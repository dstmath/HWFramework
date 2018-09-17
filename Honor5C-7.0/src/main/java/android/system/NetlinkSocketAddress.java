package android.system;

import java.net.SocketAddress;
import libcore.util.Objects;

public final class NetlinkSocketAddress extends SocketAddress {
    private final int nlGroupsMask;
    private final int nlPortId;

    public NetlinkSocketAddress() {
        this(0, 0);
    }

    public NetlinkSocketAddress(int nlPortId) {
        this(nlPortId, 0);
    }

    public NetlinkSocketAddress(int nlPortId, int nlGroupsMask) {
        this.nlPortId = nlPortId;
        this.nlGroupsMask = nlGroupsMask;
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
