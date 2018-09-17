package android.net.dhcp;

import android.net.util.NetworkConstants;
import java.net.Inet4Address;
import java.nio.ByteBuffer;

class DhcpOfferPacket extends DhcpPacket {
    private final Inet4Address mSrcIp;

    DhcpOfferPacket(int transId, short secs, boolean broadcast, Inet4Address serverAddress, Inet4Address clientIp, Inet4Address yourIp, byte[] clientMac) {
        super(transId, secs, clientIp, yourIp, INADDR_ANY, INADDR_ANY, clientMac, broadcast);
        this.mSrcIp = serverAddress;
    }

    public String toString() {
        String s = super.toString();
        String dnsServers = ", DNS servers: ";
        if (this.mDnsServers != null) {
            for (Inet4Address dnsServer : this.mDnsServers) {
                dnsServers = dnsServers + dnsServer + " ";
            }
        }
        return s + " OFFER, ip " + "xxx.xxx.xxx.xxx" + ", mask " + this.mSubnetMask + dnsServers + ", gateways " + this.mGateways + " lease time " + this.mLeaseTime + ", domain " + this.mDomainName;
    }

    public ByteBuffer buildPacket(int encap, short destUdp, short srcUdp) {
        ByteBuffer result = ByteBuffer.allocate(NetworkConstants.ETHER_MTU);
        fillInPacket(encap, this.mBroadcast ? INADDR_BROADCAST : this.mYourIp, this.mBroadcast ? INADDR_ANY : this.mSrcIp, destUdp, srcUdp, result, (byte) 2, this.mBroadcast);
        result.flip();
        return result;
    }

    void finishPacket(ByteBuffer buffer) {
        DhcpPacket.addTlv(buffer, (byte) 53, (byte) 2);
        DhcpPacket.addTlv(buffer, (byte) 54, this.mServerIdentifier);
        DhcpPacket.addTlv(buffer, (byte) 51, this.mLeaseTime);
        if (this.mLeaseTime != null) {
            DhcpPacket.addTlv(buffer, (byte) 58, Integer.valueOf(this.mLeaseTime.intValue() / 2));
        }
        DhcpPacket.addTlv(buffer, (byte) 1, this.mSubnetMask);
        DhcpPacket.addTlv(buffer, (byte) 3, this.mGateways);
        DhcpPacket.addTlv(buffer, (byte) 15, this.mDomainName);
        DhcpPacket.addTlv(buffer, (byte) 28, this.mBroadcastAddress);
        DhcpPacket.addTlv(buffer, (byte) 6, this.mDnsServers);
        DhcpPacket.addTlvEnd(buffer);
    }
}
