package com.android.server.connectivity;

import android.net.util.IpUtils;
import android.system.OsConstants;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class KeepalivePacketData {
    private static final int IPV4_HEADER_LENGTH = 20;
    private static final int UDP_HEADER_LENGTH = 8;
    public final byte[] data;
    public final InetAddress dstAddress;
    public byte[] dstMac;
    public final int dstPort;
    public final int protocol;
    public final InetAddress srcAddress;
    public final int srcPort;

    public static class InvalidPacketException extends Exception {
        public final int error;

        public InvalidPacketException(int error) {
            this.error = error;
        }
    }

    protected KeepalivePacketData(InetAddress srcAddress, int srcPort, InetAddress dstAddress, int dstPort, byte[] data) throws InvalidPacketException {
        this.srcAddress = srcAddress;
        this.dstAddress = dstAddress;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.data = data;
        if (srcAddress == null || dstAddress == null || (srcAddress.getClass().getName().equals(dstAddress.getClass().getName()) ^ 1) != 0) {
            throw new InvalidPacketException(-21);
        }
        if (this.dstAddress instanceof Inet4Address) {
            this.protocol = OsConstants.ETH_P_IP;
        } else if (this.dstAddress instanceof Inet6Address) {
            this.protocol = OsConstants.ETH_P_IPV6;
        } else {
            throw new InvalidPacketException(-21);
        }
        if (!IpUtils.isValidUdpOrTcpPort(srcPort) || (IpUtils.isValidUdpOrTcpPort(dstPort) ^ 1) != 0) {
            throw new InvalidPacketException(-22);
        }
    }

    public static KeepalivePacketData nattKeepalivePacket(InetAddress srcAddress, int srcPort, InetAddress dstAddress, int dstPort) throws InvalidPacketException {
        if (!(srcAddress instanceof Inet4Address) || ((dstAddress instanceof Inet4Address) ^ 1) != 0) {
            throw new InvalidPacketException(-21);
        } else if (dstPort != 4500) {
            throw new InvalidPacketException(-22);
        } else {
            ByteBuffer buf = ByteBuffer.allocate(29);
            buf.order(ByteOrder.BIG_ENDIAN);
            buf.putShort((short) 17664);
            buf.putShort((short) 29);
            buf.putInt(0);
            buf.put((byte) 64);
            buf.put((byte) OsConstants.IPPROTO_UDP);
            int ipChecksumOffset = buf.position();
            buf.putShort((short) 0);
            buf.put(srcAddress.getAddress());
            buf.put(dstAddress.getAddress());
            buf.putShort((short) srcPort);
            buf.putShort((short) dstPort);
            buf.putShort((short) 9);
            int udpChecksumOffset = buf.position();
            buf.putShort((short) 0);
            buf.put((byte) -1);
            buf.putShort(ipChecksumOffset, IpUtils.ipChecksum(buf, 0));
            buf.putShort(udpChecksumOffset, IpUtils.udpChecksum(buf, 0, 20));
            return new KeepalivePacketData(srcAddress, srcPort, dstAddress, dstPort, buf.array());
        }
    }
}
