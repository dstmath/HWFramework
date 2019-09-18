package android.net.dhcp;

import android.net.util.NetworkConstants;
import com.android.server.usb.descriptors.UsbDescriptor;
import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

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
            Iterator it = this.mDnsServers.iterator();
            while (it.hasNext()) {
                it.next();
                dnsServers = dnsServers + "xxx.xxx.xxx.xxx" + " ";
            }
        }
        return s + " OFFER, ip " + "xxx.xxx.xxx.xxx" + ", mask " + this.mSubnetMask + dnsServers + ", gateways " + "xxx.xxx.xxx.xxx" + " lease time " + this.mLeaseTime + ", domain " + this.mDomainName;
    }

    public ByteBuffer buildPacket(int encap, short destUdp, short srcUdp) {
        ByteBuffer result = ByteBuffer.allocate(NetworkConstants.ETHER_MTU);
        fillInPacket(encap, this.mBroadcast ? INADDR_BROADCAST : this.mYourIp, this.mBroadcast ? INADDR_ANY : this.mSrcIp, destUdp, srcUdp, result, (byte) 2, this.mBroadcast);
        result.flip();
        return result;
    }

    /* access modifiers changed from: package-private */
    public void finishPacket(ByteBuffer buffer) {
        addTlv(buffer, (byte) 53, (byte) 2);
        addTlv(buffer, (byte) 54, this.mServerIdentifier);
        addTlv(buffer, (byte) 51, this.mLeaseTime);
        if (this.mLeaseTime != null) {
            addTlv(buffer, (byte) 58, Integer.valueOf(this.mLeaseTime.intValue() / 2));
        }
        addTlv(buffer, (byte) 1, this.mSubnetMask);
        addTlv(buffer, (byte) 3, (List<Inet4Address>) this.mGateways);
        addTlv(buffer, (byte) UsbDescriptor.DESCRIPTORTYPE_BOS, this.mDomainName);
        addTlv(buffer, (byte) 28, this.mBroadcastAddress);
        addTlv(buffer, (byte) 6, (List<Inet4Address>) this.mDnsServers);
        addTlvEnd(buffer);
    }
}
