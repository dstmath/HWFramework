package android.net.dhcp;

import android.net.util.NetworkConstants;
import com.android.server.usb.descriptors.UsbDescriptor;
import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

class DhcpAckPacket extends DhcpPacket {
    private final Inet4Address mSrcIp;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    DhcpAckPacket(int transId, short secs, boolean broadcast, Inet4Address serverAddress, Inet4Address clientIp, Inet4Address yourIp, byte[] clientMac) {
        super(transId, secs, clientIp, yourIp, serverAddress, INADDR_ANY, clientMac, broadcast);
        this.mBroadcast = broadcast;
        this.mSrcIp = serverAddress;
    }

    public String toString() {
        String s = super.toString();
        String dnsServers = " DNS servers: ";
        Iterator it = this.mDnsServers.iterator();
        while (it.hasNext()) {
            it.next();
            dnsServers = dnsServers + "xxx.xxx.xxx.xxx" + " ";
        }
        return s + " ACK: your new IP " + "xxx.xxx.xxx.xxx" + ", netmask " + this.mSubnetMask + ", gateways " + "xxx.xxx.xxx.xxx" + dnsServers + ", lease time " + this.mLeaseTime;
    }

    public ByteBuffer buildPacket(int encap, short destUdp, short srcUdp) {
        ByteBuffer result = ByteBuffer.allocate(NetworkConstants.ETHER_MTU);
        fillInPacket(encap, this.mBroadcast ? INADDR_BROADCAST : this.mYourIp, this.mBroadcast ? INADDR_ANY : this.mSrcIp, destUdp, srcUdp, result, (byte) 2, this.mBroadcast);
        result.flip();
        return result;
    }

    /* access modifiers changed from: package-private */
    public void finishPacket(ByteBuffer buffer) {
        addTlv(buffer, (byte) 53, (byte) 5);
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

    private static final int getInt(Integer v) {
        if (v == null) {
            return 0;
        }
        return v.intValue();
    }
}
