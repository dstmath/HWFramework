package android.net.dhcp;

import android.net.util.NetworkConstants;
import java.net.Inet4Address;
import java.nio.ByteBuffer;

class DhcpRequestPacket extends DhcpPacket {
    DhcpRequestPacket(int transId, short secs, Inet4Address clientIp, byte[] clientMac, boolean broadcast) {
        super(transId, secs, clientIp, INADDR_ANY, INADDR_ANY, INADDR_ANY, clientMac, broadcast);
    }

    public String toString() {
        return super.toString() + " REQUEST, desired IP " + this.mRequestedIp + " from host '" + this.mHostName + "', param list length " + (this.mRequestedParams == null ? 0 : this.mRequestedParams.length);
    }

    public ByteBuffer buildPacket(int encap, short destUdp, short srcUdp) {
        ByteBuffer result = ByteBuffer.allocate(NetworkConstants.ETHER_MTU);
        fillInPacket(encap, INADDR_BROADCAST, INADDR_ANY, destUdp, srcUdp, result, (byte) 1, this.mBroadcast);
        result.flip();
        return result;
    }

    void finishPacket(ByteBuffer buffer) {
        DhcpPacket.addTlv(buffer, (byte) 53, (byte) 3);
        DhcpPacket.addTlv(buffer, (byte) 61, getClientId());
        if (!INADDR_ANY.equals(this.mRequestedIp)) {
            DhcpPacket.addTlv(buffer, (byte) 50, this.mRequestedIp);
        }
        if (!INADDR_ANY.equals(this.mServerIdentifier)) {
            DhcpPacket.addTlv(buffer, (byte) 54, this.mServerIdentifier);
        }
        addCommonClientTlvs(buffer);
        DhcpPacket.addTlv(buffer, (byte) 55, this.mRequestedParams);
        DhcpPacket.addTlvEnd(buffer);
    }
}
