package android.net.util;

import android.system.OsConstants;
import com.android.internal.midi.MidiConstants;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class IpUtils {
    private static int intAbs(short v) {
        return 65535 & v;
    }

    /* JADX INFO: Multiple debug info for r4v6 int: [D('sum' int), D('negated' int)] */
    private static int checksum(ByteBuffer buf, int seed, int start, int end) {
        int sum = seed;
        int bufPosition = buf.position();
        buf.position(start);
        ShortBuffer shortBuf = buf.asShortBuffer();
        buf.position(bufPosition);
        int numShorts = (end - start) / 2;
        for (int i = 0; i < numShorts; i++) {
            sum += intAbs(shortBuf.get(i));
        }
        int start2 = start + (numShorts * 2);
        if (end != start2) {
            short b = (short) buf.get(start2);
            if (b < 0) {
                b = (short) (b + 256);
            }
            sum += b * 256;
        }
        int sum2 = ((sum >> 16) & 65535) + (sum & 65535);
        return intAbs((short) (~((((sum2 >> 16) & 65535) + sum2) & 65535)));
    }

    private static int pseudoChecksumIPv4(ByteBuffer buf, int headerOffset, int protocol, int transportLen) {
        return protocol + transportLen + intAbs(buf.getShort(headerOffset + 12)) + intAbs(buf.getShort(headerOffset + 14)) + intAbs(buf.getShort(headerOffset + 16)) + intAbs(buf.getShort(headerOffset + 18));
    }

    private static int pseudoChecksumIPv6(ByteBuffer buf, int headerOffset, int protocol, int transportLen) {
        int partial = protocol + transportLen;
        for (int offset = 8; offset < 40; offset += 2) {
            partial += intAbs(buf.getShort(headerOffset + offset));
        }
        return partial;
    }

    private static byte ipversion(ByteBuffer buf, int headerOffset) {
        return (byte) ((buf.get(headerOffset) & -16) >> 4);
    }

    public static short ipChecksum(ByteBuffer buf, int headerOffset) {
        return (short) checksum(buf, 0, headerOffset, (((byte) (buf.get(headerOffset) & MidiConstants.STATUS_CHANNEL_MASK)) * 4) + headerOffset);
    }

    private static short transportChecksum(ByteBuffer buf, int protocol, int ipOffset, int transportOffset, int transportLen) {
        int sum;
        if (transportLen >= 0) {
            byte ver = ipversion(buf, ipOffset);
            if (ver == 4) {
                sum = pseudoChecksumIPv4(buf, ipOffset, protocol, transportLen);
            } else if (ver == 6) {
                sum = pseudoChecksumIPv6(buf, ipOffset, protocol, transportLen);
            } else {
                throw new UnsupportedOperationException("Checksum must be IPv4 or IPv6");
            }
            int sum2 = checksum(buf, sum, transportOffset, transportOffset + transportLen);
            if (protocol == OsConstants.IPPROTO_UDP && sum2 == 0) {
                sum2 = -1;
            }
            return (short) sum2;
        }
        throw new IllegalArgumentException("Transport length < 0: " + transportLen);
    }

    public static short udpChecksum(ByteBuffer buf, int ipOffset, int transportOffset) {
        return transportChecksum(buf, OsConstants.IPPROTO_UDP, ipOffset, transportOffset, intAbs(buf.getShort(transportOffset + 4)));
    }

    public static short tcpChecksum(ByteBuffer buf, int ipOffset, int transportOffset, int transportLen) {
        return transportChecksum(buf, OsConstants.IPPROTO_TCP, ipOffset, transportOffset, transportLen);
    }

    public static String addressAndPortToString(InetAddress address, int port) {
        return String.format(address instanceof Inet6Address ? "[%s]:%d" : "%s:%d", address.getHostAddress(), Integer.valueOf(port));
    }

    public static boolean isValidUdpOrTcpPort(int port) {
        return port > 0 && port < 65536;
    }
}
