package android.net.util;

import android.system.OsConstants;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class IpUtils {
    private static int checksum(java.nio.ByteBuffer r1, int r2, int r3, int r4) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.util.IpUtils.checksum(java.nio.ByteBuffer, int, int, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.util.IpUtils.checksum(java.nio.ByteBuffer, int, int, int):int");
    }

    private static int intAbs(short v) {
        return 65535 & v;
    }

    private static int pseudoChecksumIPv4(ByteBuffer buf, int headerOffset, int protocol, int transportLen) {
        return ((((protocol + transportLen) + intAbs(buf.getShort(headerOffset + 12))) + intAbs(buf.getShort(headerOffset + 14))) + intAbs(buf.getShort(headerOffset + 16))) + intAbs(buf.getShort(headerOffset + 18));
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
        return (short) checksum(buf, 0, headerOffset, (((byte) (buf.get(headerOffset) & 15)) * 4) + headerOffset);
    }

    private static short transportChecksum(ByteBuffer buf, int protocol, int ipOffset, int transportOffset, int transportLen) {
        if (transportLen < 0) {
            throw new IllegalArgumentException("Transport length < 0: " + transportLen);
        }
        int sum;
        byte ver = ipversion(buf, ipOffset);
        if (ver == 4) {
            sum = pseudoChecksumIPv4(buf, ipOffset, protocol, transportLen);
        } else if (ver == 6) {
            sum = pseudoChecksumIPv6(buf, ipOffset, protocol, transportLen);
        } else {
            throw new UnsupportedOperationException("Checksum must be IPv4 or IPv6");
        }
        sum = checksum(buf, sum, transportOffset, transportOffset + transportLen);
        if (protocol == OsConstants.IPPROTO_UDP && sum == 0) {
            sum = -1;
        }
        return (short) sum;
    }

    public static short udpChecksum(ByteBuffer buf, int ipOffset, int transportOffset) {
        return transportChecksum(buf, OsConstants.IPPROTO_UDP, ipOffset, transportOffset, intAbs(buf.getShort(transportOffset + 4)));
    }

    public static short tcpChecksum(ByteBuffer buf, int ipOffset, int transportOffset, int transportLen) {
        return transportChecksum(buf, OsConstants.IPPROTO_TCP, ipOffset, transportOffset, transportLen);
    }

    public static String addressAndPortToString(InetAddress address, int port) {
        return String.format(address instanceof Inet6Address ? "[%s]:%d" : "%s:%d", new Object[]{address.getHostAddress(), Integer.valueOf(port)});
    }

    public static boolean isValidUdpOrTcpPort(int port) {
        return port > 0 && port < DumpState.DUMP_INSTALLS;
    }
}
