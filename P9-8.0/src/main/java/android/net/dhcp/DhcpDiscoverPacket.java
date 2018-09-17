package android.net.dhcp;

import android.net.util.NetworkConstants;
import java.nio.ByteBuffer;

class DhcpDiscoverPacket extends DhcpPacket {
    DhcpDiscoverPacket(int transId, short secs, byte[] clientMac, boolean broadcast) {
        super(transId, secs, INADDR_ANY, INADDR_ANY, INADDR_ANY, INADDR_ANY, clientMac, broadcast);
    }

    public String toString() {
        return super.toString() + " DISCOVER " + (this.mBroadcast ? "broadcast " : "unicast ");
    }

    public ByteBuffer buildPacket(int encap, short destUdp, short srcUdp) {
        ByteBuffer result = ByteBuffer.allocate(NetworkConstants.ETHER_MTU);
        fillInPacket(encap, INADDR_BROADCAST, INADDR_ANY, destUdp, srcUdp, result, (byte) 1, this.mBroadcast);
        result.flip();
        return result;
    }

    void finishPacket(ByteBuffer buffer) {
        DhcpPacket.addTlv(buffer, (byte) 53, (byte) 1);
        DhcpPacket.addTlv(buffer, (byte) 61, getClientId());
        addCommonClientTlvs(buffer);
        DhcpPacket.addTlv(buffer, (byte) 55, this.mRequestedParams);
        DhcpPacket.addTlvEnd(buffer);
    }
}
