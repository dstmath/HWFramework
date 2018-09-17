package android.net.ip;

import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.TrafficStats;
import android.net.util.NetworkConstants;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructTimeval;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.io.IoBridge;

public class RouterAdvertisementDaemon {
    private static final byte[] ALL_NODES = new byte[]{(byte) -1, (byte) 2, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1};
    private static final int DAY_IN_SECONDS = 86400;
    private static final int DEFAULT_LIFETIME = 3600;
    private static final byte ICMPV6_ND_ROUTER_ADVERT = asByte(NetworkConstants.ICMPV6_ROUTER_ADVERTISEMENT);
    private static final byte ICMPV6_ND_ROUTER_SOLICIT = asByte(NetworkConstants.ICMPV6_ROUTER_SOLICITATION);
    private static final int MAX_RTR_ADV_INTERVAL_SEC = 600;
    private static final int MAX_URGENT_RTR_ADVERTISEMENTS = 5;
    private static final int MIN_DELAY_BETWEEN_RAS_SEC = 3;
    private static final int MIN_RA_HEADER_SIZE = 16;
    private static final int MIN_RTR_ADV_INTERVAL_SEC = 300;
    private static final String TAG = RouterAdvertisementDaemon.class.getSimpleName();
    private final InetSocketAddress mAllNodes;
    @GuardedBy("mLock")
    private final DeprecatedInfoTracker mDeprecatedInfoTracker;
    private final byte[] mHwAddr;
    private final int mIfIndex;
    private final String mIfName;
    private final Object mLock = new Object();
    private volatile MulticastTransmitter mMulticastTransmitter;
    @GuardedBy("mLock")
    private final byte[] mRA = new byte[NetworkConstants.IPV6_MIN_MTU];
    @GuardedBy("mLock")
    private int mRaLength;
    @GuardedBy("mLock")
    private RaParams mRaParams;
    private volatile FileDescriptor mSocket;
    private volatile UnicastResponder mUnicastResponder;

    private static class DeprecatedInfoTracker {
        private final HashMap<Inet6Address, Integer> mDnses;
        private final HashMap<IpPrefix, Integer> mPrefixes;

        /* synthetic */ DeprecatedInfoTracker(DeprecatedInfoTracker -this0) {
            this();
        }

        private DeprecatedInfoTracker() {
            this.mPrefixes = new HashMap();
            this.mDnses = new HashMap();
        }

        Set<IpPrefix> getPrefixes() {
            return this.mPrefixes.keySet();
        }

        void putPrefixes(Set<IpPrefix> prefixes) {
            for (IpPrefix ipp : prefixes) {
                this.mPrefixes.put(ipp, Integer.valueOf(5));
            }
        }

        void removePrefixes(Set<IpPrefix> prefixes) {
            for (IpPrefix ipp : prefixes) {
                this.mPrefixes.remove(ipp);
            }
        }

        Set<Inet6Address> getDnses() {
            return this.mDnses.keySet();
        }

        void putDnses(Set<Inet6Address> dnses) {
            for (Inet6Address dns : dnses) {
                this.mDnses.put(dns, Integer.valueOf(5));
            }
        }

        void removeDnses(Set<Inet6Address> dnses) {
            for (Inet6Address dns : dnses) {
                this.mDnses.remove(dns);
            }
        }

        boolean isEmpty() {
            return this.mPrefixes.isEmpty() ? this.mDnses.isEmpty() : false;
        }

        private boolean decrementCounters() {
            return decrementCounter(this.mPrefixes) | decrementCounter(this.mDnses);
        }

        private <T> boolean decrementCounter(HashMap<T, Integer> map) {
            boolean removed = false;
            Iterator<Entry<T, Integer>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<T, Integer> kv = (Entry) it.next();
                if (((Integer) kv.getValue()).intValue() == 0) {
                    it.remove();
                    removed = true;
                } else {
                    kv.setValue(Integer.valueOf(((Integer) kv.getValue()).intValue() - 1));
                }
            }
            return removed;
        }
    }

    private final class MulticastTransmitter extends Thread {
        private final Random mRandom;
        private final AtomicInteger mUrgentAnnouncements;

        /* synthetic */ MulticastTransmitter(RouterAdvertisementDaemon this$0, MulticastTransmitter -this1) {
            this();
        }

        private MulticastTransmitter() {
            this.mRandom = new Random();
            this.mUrgentAnnouncements = new AtomicInteger(0);
        }

        public void run() {
            while (RouterAdvertisementDaemon.this.isSocketValid()) {
                try {
                    Thread.sleep(getNextMulticastTransmitDelayMs());
                } catch (InterruptedException e) {
                }
                RouterAdvertisementDaemon.this.maybeSendRA(RouterAdvertisementDaemon.this.mAllNodes);
                synchronized (RouterAdvertisementDaemon.this.mLock) {
                    if (RouterAdvertisementDaemon.this.mDeprecatedInfoTracker.decrementCounters()) {
                        RouterAdvertisementDaemon.this.assembleRaLocked();
                    }
                }
            }
        }

        public void hup() {
            this.mUrgentAnnouncements.set(4);
            interrupt();
        }

        /* JADX WARNING: Missing block: B:14:0x002c, code:
            if (r6.mUrgentAnnouncements.getAndDecrement() > 0) goto L_0x0030;
     */
        /* JADX WARNING: Missing block: B:15:0x002e, code:
            if (r0 == false) goto L_0x0035;
     */
        /* JADX WARNING: Missing block: B:17:0x0031, code:
            return 3;
     */
        /* JADX WARNING: Missing block: B:22:0x003d, code:
            return r6.mRandom.nextInt(300) + 300;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int getNextMulticastTransmitDelaySec() {
            synchronized (RouterAdvertisementDaemon.this.mLock) {
                if (RouterAdvertisementDaemon.this.mRaLength < 16) {
                    return RouterAdvertisementDaemon.DAY_IN_SECONDS;
                }
                boolean deprecationInProgress = RouterAdvertisementDaemon.this.mDeprecatedInfoTracker.isEmpty() ^ 1;
            }
        }

        private long getNextMulticastTransmitDelayMs() {
            return ((long) getNextMulticastTransmitDelaySec()) * 1000;
        }
    }

    public static class RaParams {
        public HashSet<Inet6Address> dnses;
        public boolean hasDefaultRoute;
        public int mtu;
        public HashSet<IpPrefix> prefixes;

        public RaParams() {
            this.hasDefaultRoute = false;
            this.mtu = NetworkConstants.IPV6_MIN_MTU;
            this.prefixes = new HashSet();
            this.dnses = new HashSet();
        }

        public RaParams(RaParams other) {
            this.hasDefaultRoute = other.hasDefaultRoute;
            this.mtu = other.mtu;
            this.prefixes = (HashSet) other.prefixes.clone();
            this.dnses = (HashSet) other.dnses.clone();
        }

        public static RaParams getDeprecatedRaParams(RaParams oldRa, RaParams newRa) {
            RaParams newlyDeprecated = new RaParams();
            if (oldRa != null) {
                for (IpPrefix ipp : oldRa.prefixes) {
                    if (newRa == null || (newRa.prefixes.contains(ipp) ^ 1) != 0) {
                        newlyDeprecated.prefixes.add(ipp);
                    }
                }
                for (Inet6Address dns : oldRa.dnses) {
                    if (newRa == null || (newRa.dnses.contains(dns) ^ 1) != 0) {
                        newlyDeprecated.dnses.add(dns);
                    }
                }
            }
            return newlyDeprecated;
        }
    }

    private final class UnicastResponder extends Thread {
        private final byte[] mSolication;
        private final InetSocketAddress solicitor;

        /* synthetic */ UnicastResponder(RouterAdvertisementDaemon this$0, UnicastResponder -this1) {
            this();
        }

        private UnicastResponder() {
            this.solicitor = new InetSocketAddress();
            this.mSolication = new byte[NetworkConstants.IPV6_MIN_MTU];
        }

        /* JADX WARNING: Removed duplicated region for block: B:8:0x0031 A:{Splitter: B:2:0x0008, ExcHandler: android.system.ErrnoException (r6_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:8:0x0031, code:
            r6 = move-exception;
     */
        /* JADX WARNING: Missing block: B:10:0x0038, code:
            if (android.net.ip.RouterAdvertisementDaemon.-wrap0(r8.this$0) != false) goto L_0x003a;
     */
        /* JADX WARNING: Missing block: B:11:0x003a, code:
            android.util.Log.e(android.net.ip.RouterAdvertisementDaemon.-get1(), "recvfrom error: " + r6);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            while (RouterAdvertisementDaemon.this.isSocketValid()) {
                try {
                    if (Os.recvfrom(RouterAdvertisementDaemon.this.mSocket, this.mSolication, 0, this.mSolication.length, 0, this.solicitor) >= 1 && this.mSolication[0] == RouterAdvertisementDaemon.ICMPV6_ND_ROUTER_SOLICIT) {
                        RouterAdvertisementDaemon.this.maybeSendRA(this.solicitor);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    public RouterAdvertisementDaemon(String ifname, int ifindex, byte[] hwaddr) {
        this.mIfName = ifname;
        this.mIfIndex = ifindex;
        this.mHwAddr = hwaddr;
        this.mAllNodes = new InetSocketAddress(getAllNodesForScopeId(this.mIfIndex), 0);
        this.mDeprecatedInfoTracker = new DeprecatedInfoTracker();
    }

    public void buildNewRa(RaParams deprecatedParams, RaParams newParams) {
        synchronized (this.mLock) {
            if (deprecatedParams != null) {
                this.mDeprecatedInfoTracker.putPrefixes(deprecatedParams.prefixes);
                this.mDeprecatedInfoTracker.putDnses(deprecatedParams.dnses);
            }
            if (newParams != null) {
                this.mDeprecatedInfoTracker.removePrefixes(newParams.prefixes);
                this.mDeprecatedInfoTracker.removeDnses(newParams.dnses);
            }
            this.mRaParams = newParams;
            assembleRaLocked();
        }
        maybeNotifyMulticastTransmitter();
    }

    public boolean start() {
        if (!createSocket()) {
            return false;
        }
        this.mMulticastTransmitter = new MulticastTransmitter(this, null);
        this.mMulticastTransmitter.start();
        this.mUnicastResponder = new UnicastResponder(this, null);
        this.mUnicastResponder.start();
        return true;
    }

    public void stop() {
        closeSocket();
        this.mMulticastTransmitter = null;
        this.mUnicastResponder = null;
    }

    private void assembleRaLocked() {
        ByteBuffer ra = ByteBuffer.wrap(this.mRA);
        ra.order(ByteOrder.BIG_ENDIAN);
        boolean shouldSendRA = false;
        try {
            boolean z;
            if (this.mRaParams != null) {
                z = this.mRaParams.hasDefaultRoute;
            } else {
                z = false;
            }
            putHeader(ra, z);
            putSlla(ra, this.mHwAddr);
            this.mRaLength = ra.position();
            if (this.mRaParams != null) {
                putMtu(ra, this.mRaParams.mtu);
                this.mRaLength = ra.position();
                for (IpPrefix ipp : this.mRaParams.prefixes) {
                    putPio(ra, ipp, DEFAULT_LIFETIME, DEFAULT_LIFETIME);
                    this.mRaLength = ra.position();
                    shouldSendRA = true;
                }
                if (this.mRaParams.dnses.size() > 0) {
                    putRdnss(ra, this.mRaParams.dnses, DEFAULT_LIFETIME);
                    this.mRaLength = ra.position();
                    shouldSendRA = true;
                }
            }
            for (IpPrefix ipp2 : this.mDeprecatedInfoTracker.getPrefixes()) {
                putPio(ra, ipp2, 0, 0);
                this.mRaLength = ra.position();
                shouldSendRA = true;
            }
            Set<Inet6Address> deprecatedDnses = this.mDeprecatedInfoTracker.getDnses();
            if (!deprecatedDnses.isEmpty()) {
                putRdnss(ra, deprecatedDnses, 0);
                this.mRaLength = ra.position();
                shouldSendRA = true;
            }
        } catch (BufferOverflowException e) {
            Log.e(TAG, "Could not construct new RA: " + e);
        }
        if (!shouldSendRA) {
            this.mRaLength = 0;
        }
    }

    private void maybeNotifyMulticastTransmitter() {
        MulticastTransmitter m = this.mMulticastTransmitter;
        if (m != null) {
            m.hup();
        }
    }

    private static Inet6Address getAllNodesForScopeId(int scopeId) {
        try {
            return Inet6Address.getByAddress("ff02::1", ALL_NODES, scopeId);
        } catch (UnknownHostException uhe) {
            Log.wtf(TAG, "Failed to construct ff02::1 InetAddress: " + uhe);
            return null;
        }
    }

    private static byte asByte(int value) {
        return (byte) value;
    }

    private static short asShort(int value) {
        return (short) value;
    }

    private static void putHeader(ByteBuffer ra, boolean hasDefaultRoute) {
        byte asByte;
        short asShort;
        ByteBuffer put = ra.put(ICMPV6_ND_ROUTER_ADVERT).put(asByte(0)).putShort(asShort(0)).put((byte) 64);
        if (hasDefaultRoute) {
            asByte = asByte(8);
        } else {
            asByte = asByte(0);
        }
        put = put.put(asByte);
        if (hasDefaultRoute) {
            asShort = asShort(DEFAULT_LIFETIME);
        } else {
            asShort = asShort(0);
        }
        put.putShort(asShort).putInt(0).putInt(0);
    }

    private static void putSlla(ByteBuffer ra, byte[] slla) {
        if (slla != null && slla.length == 6) {
            ra.put((byte) 1).put((byte) 1).put(slla);
        }
    }

    private static void putExpandedFlagsOption(ByteBuffer ra) {
        ra.put((byte) 26).put((byte) 1).putShort(asShort(0)).putInt(0);
    }

    private static void putMtu(ByteBuffer ra, int mtu) {
        ByteBuffer putShort = ra.put((byte) 5).put((byte) 1).putShort(asShort(0));
        if (mtu < NetworkConstants.IPV6_MIN_MTU) {
            mtu = NetworkConstants.IPV6_MIN_MTU;
        }
        putShort.putInt(mtu);
    }

    private static void putPio(ByteBuffer ra, IpPrefix ipp, int validTime, int preferredTime) {
        int prefixLength = ipp.getPrefixLength();
        if (prefixLength == 64) {
            if (validTime < 0) {
                validTime = 0;
            }
            if (preferredTime < 0) {
                preferredTime = 0;
            }
            if (preferredTime > validTime) {
                preferredTime = validTime;
            }
            ra.put((byte) 3).put((byte) 4).put(asByte(prefixLength)).put(asByte(192)).putInt(validTime).putInt(preferredTime).putInt(0).put(ipp.getAddress().getAddress());
        }
    }

    private static void putRio(ByteBuffer ra, IpPrefix ipp) {
        int prefixLength = ipp.getPrefixLength();
        if (prefixLength <= 64) {
            int i = prefixLength == 0 ? 1 : prefixLength <= 8 ? 2 : 3;
            byte RIO_NUM_8OCTETS = asByte(i);
            byte[] addr = ipp.getAddress().getAddress();
            ra.put((byte) 24).put(RIO_NUM_8OCTETS).put(asByte(prefixLength)).put(asByte(24)).putInt(DEFAULT_LIFETIME);
            if (prefixLength > 0) {
                ra.put(addr, 0, prefixLength <= 64 ? 8 : 16);
            }
        }
    }

    private static void putRdnss(ByteBuffer ra, Set<Inet6Address> dnses, int lifetime) {
        HashSet<Inet6Address> filteredDnses = new HashSet();
        for (Inet6Address dns : dnses) {
            if (new LinkAddress(dns, 64).isGlobalPreferred()) {
                filteredDnses.add(dns);
            }
        }
        if (!filteredDnses.isEmpty()) {
            ra.put((byte) 25).put(asByte((dnses.size() * 2) + 1)).putShort(asShort(0)).putInt(lifetime);
            for (Inet6Address dns2 : filteredDnses) {
                ra.put(dns2.getAddress());
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x003f A:{Splitter: B:1:0x0008, ExcHandler: android.system.ErrnoException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x003f, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:7:?, code:
            android.util.Log.e(TAG, "Failed to create RA daemon socket: " + r1);
     */
    /* JADX WARNING: Missing block: B:8:0x0059, code:
            android.net.TrafficStats.setThreadStatsTag(r2);
     */
    /* JADX WARNING: Missing block: B:9:0x005d, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean createSocket() {
        int oldTag = TrafficStats.getAndSetThreadStatsTag(-248);
        try {
            this.mSocket = Os.socket(OsConstants.AF_INET6, OsConstants.SOCK_RAW, OsConstants.IPPROTO_ICMPV6);
            Os.setsockoptTimeval(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(300));
            Os.setsockoptIfreq(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_BINDTODEVICE, this.mIfName);
            NetworkUtils.protectFromVpn(this.mSocket);
            NetworkUtils.setupRaSocket(this.mSocket, this.mIfIndex);
            TrafficStats.setThreadStatsTag(oldTag);
            return true;
        } catch (Exception e) {
        } catch (Throwable th) {
            TrafficStats.setThreadStatsTag(oldTag);
            throw th;
        }
    }

    private void closeSocket() {
        if (this.mSocket != null) {
            try {
                IoBridge.closeAndSignalBlockedThreads(this.mSocket);
            } catch (IOException e) {
            }
        }
        this.mSocket = null;
    }

    private boolean isSocketValid() {
        FileDescriptor s = this.mSocket;
        return s != null ? s.valid() : false;
    }

    private boolean isSuitableDestination(InetSocketAddress dest) {
        boolean z = true;
        if (this.mAllNodes.equals(dest)) {
            return true;
        }
        InetAddress destip = dest.getAddress();
        if (!(destip instanceof Inet6Address) || !destip.isLinkLocalAddress()) {
            z = false;
        } else if (((Inet6Address) destip).getScopeId() != this.mIfIndex) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0049 A:{Splitter: B:4:0x000c, ExcHandler: android.system.ErrnoException (r6_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:18:0x0024, code:
            android.util.Log.d(TAG, "RA sendto " + r9.getAddress().getHostAddress());
     */
    /* JADX WARNING: Missing block: B:19:0x0045, code:
            return;
     */
    /* JADX WARNING: Missing block: B:23:0x0049, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:25:0x004e, code:
            if (isSocketValid() != false) goto L_0x0050;
     */
    /* JADX WARNING: Missing block: B:26:0x0050, code:
            android.util.Log.e(TAG, "sendto error: " + r6);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void maybeSendRA(InetSocketAddress dest) {
        SocketAddress dest2;
        if (dest2 == null || (isSuitableDestination(dest2) ^ 1) != 0) {
            dest2 = this.mAllNodes;
        }
        try {
            synchronized (this.mLock) {
                if (this.mRaLength < 16) {
                    return;
                }
                Os.sendto(this.mSocket, this.mRA, 0, this.mRaLength, 0, dest2);
            }
        } catch (Exception e) {
        }
    }
}
