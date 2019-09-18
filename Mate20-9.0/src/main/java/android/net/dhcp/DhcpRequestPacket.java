package android.net.dhcp;

import android.net.util.NetworkConstants;
import java.net.Inet4Address;
import java.nio.ByteBuffer;

class DhcpRequestPacket extends DhcpPacket {
    DhcpRequestPacket(int transId, short secs, Inet4Address clientIp, byte[] clientMac, boolean broadcast) {
        super(transId, secs, clientIp, INADDR_ANY, INADDR_ANY, INADDR_ANY, clientMac, broadcast);
    }

    public String toString() {
        String s = super.toString();
        StringBuilder sb = new StringBuilder();
        sb.append(s);
        sb.append(" REQUEST, desired IP ");
        sb.append(this.mRequestedIp);
        sb.append(" from host '");
        sb.append(this.mHostName);
        sb.append("', param list length ");
        sb.append(this.mRequestedParams == null ? 0 : this.mRequestedParams.length);
        return sb.toString();
    }

    public ByteBuffer buildPacket(int encap, short destUdp, short srcUdp) {
        ByteBuffer result = ByteBuffer.allocate(NetworkConstants.ETHER_MTU);
        fillInPacket(encap, INADDR_BROADCAST, INADDR_ANY, destUdp, srcUdp, result, (byte) 1, this.mBroadcast);
        result.flip();
        return result;
    }

    /* access modifiers changed from: package-private */
    public void finishPacket(ByteBuffer buffer) {
        addTlv(buffer, (byte) 53, (byte) 3);
        addTlv(buffer, (byte) 61, getClientId());
        if (!INADDR_ANY.equals(this.mRequestedIp)) {
            addTlv(buffer, (byte) 50, this.mRequestedIp);
        }
        if (!INADDR_ANY.equals(this.mServerIdentifier)) {
            addTlv(buffer, (byte) 54, this.mServerIdentifier);
        }
        addCommonClientTlvs(buffer);
        addTlv(buffer, (byte) 55, this.mRequestedParams);
        addTlvEnd(buffer);
    }
}
