package android.net.util;

import android.net.InetAddresses;
import android.net.Network;
import android.net.util.DnsUtils;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import com.android.internal.midi.MidiConstants;
import com.android.internal.util.BitUtils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import libcore.io.IoUtils;

public class DnsUtils {
    private static final int CHAR_BIT = 8;
    public static final int IPV6_ADDR_SCOPE_GLOBAL = 14;
    public static final int IPV6_ADDR_SCOPE_LINKLOCAL = 2;
    public static final int IPV6_ADDR_SCOPE_NODELOCAL = 1;
    public static final int IPV6_ADDR_SCOPE_SITELOCAL = 5;
    private static final String TAG = "DnsUtils";
    private static final Comparator<SortableAddress> sRfc6724Comparator = new Rfc6724Comparator();

    public static class Rfc6724Comparator implements Comparator<SortableAddress> {
        public int compare(SortableAddress span1, SortableAddress span2) {
            if (span1.hasSrcAddr != span2.hasSrcAddr) {
                return span2.hasSrcAddr - span1.hasSrcAddr;
            }
            if (span1.scopeMatch != span2.scopeMatch) {
                return span2.scopeMatch - span1.scopeMatch;
            }
            if (span1.labelMatch != span2.labelMatch) {
                return span2.labelMatch - span1.labelMatch;
            }
            if (span1.precedence != span2.precedence) {
                return span2.precedence - span1.precedence;
            }
            if (span1.scope != span2.scope) {
                return span1.scope - span2.scope;
            }
            if (span1.prefixMatchLen != span2.prefixMatchLen) {
                return span2.prefixMatchLen - span1.prefixMatchLen;
            }
            return 0;
        }
    }

    public static class SortableAddress {
        public final InetAddress address;
        public final int hasSrcAddr;
        public final int label;
        public final int labelMatch;
        public final int precedence;
        public final int prefixMatchLen;
        public final int scope;
        public final int scopeMatch;

        public SortableAddress(InetAddress addr, InetAddress srcAddr) {
            this.address = addr;
            int i = 1;
            this.hasSrcAddr = srcAddr != null ? 1 : 0;
            this.label = DnsUtils.findLabel(addr);
            this.scope = DnsUtils.findScope(addr);
            this.precedence = DnsUtils.findPrecedence(addr);
            this.labelMatch = (srcAddr == null || this.label != DnsUtils.findLabel(srcAddr)) ? 0 : 1;
            this.scopeMatch = (srcAddr == null || this.scope != DnsUtils.findScope(srcAddr)) ? 0 : i;
            if (!DnsUtils.isIpv6Address(addr) || !DnsUtils.isIpv6Address(srcAddr)) {
                this.prefixMatchLen = 0;
            } else {
                this.prefixMatchLen = DnsUtils.compareIpv6PrefixMatchLen(srcAddr, addr);
            }
        }
    }

    public static List<InetAddress> rfc6724Sort(Network network, List<InetAddress> answers) {
        List<SortableAddress> sortableAnswerList = new ArrayList<>();
        answers.forEach(new Consumer(sortableAnswerList, network) {
            /* class android.net.util.$$Lambda$DnsUtils$E7rjA1PKdcqMJSVvye8jaivYDec */
            private final /* synthetic */ List f$0;
            private final /* synthetic */ Network f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                InetAddress inetAddress;
                this.f$0.add(new DnsUtils.SortableAddress(inetAddress, DnsUtils.findSrcAddress(this.f$1, (InetAddress) obj)));
            }
        });
        Collections.sort(sortableAnswerList, sRfc6724Comparator);
        List<InetAddress> sortedAnswers = new ArrayList<>();
        sortableAnswerList.forEach(new Consumer(sortedAnswers) {
            /* class android.net.util.$$Lambda$DnsUtils$GlRZOd_k4dipl4wcKx5eyR_B_sU */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.add(((DnsUtils.SortableAddress) obj).address);
            }
        });
        return sortedAnswers;
    }

    private static InetAddress findSrcAddress(Network network, InetAddress addr) {
        int domain;
        if (isIpv4Address(addr)) {
            domain = OsConstants.AF_INET;
        } else if (!isIpv6Address(addr)) {
            return null;
        } else {
            domain = OsConstants.AF_INET6;
        }
        try {
            FileDescriptor socket = Os.socket(domain, OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP);
            if (network != null) {
                try {
                    network.bindSocket(socket);
                } catch (ErrnoException | IOException e) {
                    IoUtils.closeQuietly(socket);
                    return null;
                } catch (Throwable th) {
                    IoUtils.closeQuietly(socket);
                    throw th;
                }
            }
            Os.connect(socket, new InetSocketAddress(addr, 0));
            InetAddress address = ((InetSocketAddress) Os.getsockname(socket)).getAddress();
            IoUtils.closeQuietly(socket);
            return address;
        } catch (ErrnoException e2) {
            Log.e(TAG, "findSrcAddress.");
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static int findLabel(InetAddress addr) {
        if (isIpv4Address(addr)) {
            return 4;
        }
        if (!isIpv6Address(addr)) {
            return 1;
        }
        if (addr.isLoopbackAddress()) {
            return 0;
        }
        if (isIpv6Address6To4(addr)) {
            return 2;
        }
        if (isIpv6AddressTeredo(addr)) {
            return 5;
        }
        if (isIpv6AddressULA(addr)) {
            return 13;
        }
        if (((Inet6Address) addr).isIPv4CompatibleAddress()) {
            return 3;
        }
        if (addr.isSiteLocalAddress()) {
            return 11;
        }
        if (isIpv6Address6Bone(addr)) {
            return 12;
        }
        return 1;
    }

    /* access modifiers changed from: private */
    public static boolean isIpv6Address(InetAddress addr) {
        return addr instanceof Inet6Address;
    }

    private static boolean isIpv4Address(InetAddress addr) {
        return addr instanceof Inet4Address;
    }

    private static boolean isIpv6Address6To4(InetAddress addr) {
        if (!isIpv6Address(addr)) {
            return false;
        }
        byte[] byteAddr = addr.getAddress();
        if (byteAddr[0] == 32 && byteAddr[1] == 2) {
            return true;
        }
        return false;
    }

    private static boolean isIpv6AddressTeredo(InetAddress addr) {
        if (!isIpv6Address(addr)) {
            return false;
        }
        byte[] byteAddr = addr.getAddress();
        if (byteAddr[0] == 32 && byteAddr[1] == 1 && byteAddr[2] == 0 && byteAddr[3] == 0) {
            return true;
        }
        return false;
    }

    private static boolean isIpv6AddressULA(InetAddress addr) {
        return isIpv6Address(addr) && (addr.getAddress()[0] & MidiConstants.STATUS_ACTIVE_SENSING) == 252;
    }

    private static boolean isIpv6Address6Bone(InetAddress addr) {
        if (!isIpv6Address(addr)) {
            return false;
        }
        byte[] byteAddr = addr.getAddress();
        if (byteAddr[0] == 63 && byteAddr[1] == -2) {
            return true;
        }
        return false;
    }

    private static int getIpv6MulticastScope(InetAddress addr) {
        if (!isIpv6Address(addr)) {
            return 0;
        }
        return addr.getAddress()[1] & MidiConstants.STATUS_CHANNEL_MASK;
    }

    /* access modifiers changed from: private */
    public static int findScope(InetAddress addr) {
        if (isIpv6Address(addr)) {
            if (addr.isMulticastAddress()) {
                return getIpv6MulticastScope(addr);
            }
            if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
                return 2;
            }
            if (addr.isSiteLocalAddress()) {
                return 5;
            }
            return 14;
        } else if (isIpv4Address(addr)) {
            return (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) ? 2 : 14;
        } else {
            return 1;
        }
    }

    /* access modifiers changed from: private */
    public static int findPrecedence(InetAddress addr) {
        if (isIpv4Address(addr)) {
            return 35;
        }
        if (!isIpv6Address(addr)) {
            return 1;
        }
        if (addr.isLoopbackAddress()) {
            return 50;
        }
        if (isIpv6Address6To4(addr)) {
            return 30;
        }
        if (isIpv6AddressTeredo(addr)) {
            return 5;
        }
        if (isIpv6AddressULA(addr)) {
            return 3;
        }
        if (((Inet6Address) addr).isIPv4CompatibleAddress() || addr.isSiteLocalAddress() || isIpv6Address6Bone(addr)) {
            return 1;
        }
        return 40;
    }

    /* access modifiers changed from: private */
    public static int compareIpv6PrefixMatchLen(InetAddress srcAddr, InetAddress dstAddr) {
        byte[] srcByte = srcAddr.getAddress();
        byte[] dstByte = dstAddr.getAddress();
        if (srcByte.length != dstByte.length) {
            return 0;
        }
        for (int i = 0; i < dstByte.length; i++) {
            if (srcByte[i] != dstByte[i]) {
                return (i * 8) + (Integer.numberOfLeadingZeros(BitUtils.uint8(srcByte[i]) ^ BitUtils.uint8(dstByte[i])) - 24);
            }
        }
        return dstByte.length * 8;
    }

    public static boolean haveIpv4(Network network) {
        return checkConnectivity(network, OsConstants.AF_INET, new InetSocketAddress(InetAddresses.parseNumericAddress("8.8.8.8"), 0));
    }

    public static boolean haveIpv6(Network network) {
        return checkConnectivity(network, OsConstants.AF_INET6, new InetSocketAddress(InetAddresses.parseNumericAddress("2000::"), 0));
    }

    private static boolean checkConnectivity(Network network, int domain, SocketAddress addr) {
        try {
            FileDescriptor socket = Os.socket(domain, OsConstants.SOCK_DGRAM, OsConstants.IPPROTO_UDP);
            if (network != null) {
                try {
                    network.bindSocket(socket);
                } catch (ErrnoException | IOException e) {
                    IoUtils.closeQuietly(socket);
                    return false;
                } catch (Throwable th) {
                    IoUtils.closeQuietly(socket);
                    throw th;
                }
            }
            Os.connect(socket, addr);
            IoUtils.closeQuietly(socket);
            return true;
        } catch (ErrnoException e2) {
            return false;
        }
    }
}
