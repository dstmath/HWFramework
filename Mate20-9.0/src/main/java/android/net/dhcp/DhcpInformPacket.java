package android.net.dhcp;

import android.net.util.NetworkConstants;
import java.net.Inet4Address;
import java.nio.ByteBuffer;

class DhcpInformPacket extends DhcpPacket {
    DhcpInformPacket(int transId, short secs, Inet4Address clientIp, Inet4Address yourIp, Inet4Address nextIp, Inet4Address relayIp, byte[] clientMac) {
        super(transId, secs, clientIp, yourIp, nextIp, relayIp, clientMac, false);
    }

    public String toString() {
        String s = super.toString();
        return s + " INFORM";
    }

    public ByteBuffer buildPacket(int encap, short destUdp, short srcUdp) {
        ByteBuffer result = ByteBuffer.allocate(NetworkConstants.ETHER_MTU);
        fillInPacket(encap, this.mClientIp, this.mYourIp, destUdp, srcUdp, result, (byte) 1, false);
        result.flip();
        return result;
    }

    /* access modifiers changed from: package-private */
    public void finishPacket(ByteBuffer buffer) {
        addTlv(buffer, (byte) 53, (byte) 8);
        addTlv(buffer, (byte) 61, getClientId());
        addCommonClientTlvs(buffer);
        addTlv(buffer, (byte) 55, this.mRequestedParams);
        addTlvEnd(buffer);
    }
}
