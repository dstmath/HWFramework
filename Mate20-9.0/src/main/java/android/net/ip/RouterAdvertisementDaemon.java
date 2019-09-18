package android.net.ip;

import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.TrafficStats;
import android.net.util.InterfaceParams;
import android.net.util.NetworkConstants;
import android.system.ErrnoException;
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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.io.IoBridge;

public class RouterAdvertisementDaemon {
    private static final byte[] ALL_NODES = {-1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
    private static final int DAY_IN_SECONDS = 86400;
    private static final int DEFAULT_LIFETIME = 3600;
    private static final byte ICMPV6_ND_ROUTER_ADVERT = asByte(NetworkConstants.ICMPV6_ROUTER_ADVERTISEMENT);
    /* access modifiers changed from: private */
    public static final byte ICMPV6_ND_ROUTER_SOLICIT = asByte(NetworkConstants.ICMPV6_ROUTER_SOLICITATION);
    private static final int MAX_RTR_ADV_INTERVAL_SEC = 600;
    private static final int MAX_URGENT_RTR_ADVERTISEMENTS = 5;
    private static final int MIN_DELAY_BETWEEN_RAS_SEC = 3;
    private static final int MIN_RA_HEADER_SIZE = 16;
    private static final int MIN_RTR_ADV_INTERVAL_SEC = 300;
    /* access modifiers changed from: private */
    public static final String TAG = RouterAdvertisementDaemon.class.getSimpleName();
    /* access modifiers changed from: private */
    public final InetSocketAddress mAllNodes;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final DeprecatedInfoTracker mDeprecatedInfoTracker;
    private final InterfaceParams mInterface;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private volatile MulticastTransmitter mMulticastTransmitter;
    @GuardedBy("mLock")
    private final byte[] mRA = new byte[1280];
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public int mRaLength;
    @GuardedBy("mLock")
    private RaParams mRaParams;
    /* access modifiers changed from: private */
    public volatile FileDescriptor mSocket;
    private volatile UnicastResponder mUnicastResponder;

    private static class DeprecatedInfoTracker {
        private final HashMap<Inet6Address, Integer> mDnses;
        private final HashMap<IpPrefix, Integer> mPrefixes;

        private DeprecatedInfoTracker() {
            this.mPrefixes = new HashMap<>();
            this.mDnses = new HashMap<>();
        }

        /* access modifiers changed from: package-private */
        public Set<IpPrefix> getPrefixes() {
            return this.mPrefixes.keySet();
        }

        /* access modifiers changed from: package-private */
        public void putPrefixes(Set<IpPrefix> prefixes) {
            for (IpPrefix ipp : prefixes) {
                this.mPrefixes.put(ipp, 5);
            }
        }

        /* access modifiers changed from: package-private */
        public void removePrefixes(Set<IpPrefix> prefixes) {
            for (IpPrefix ipp : prefixes) {
                this.mPrefixes.remove(ipp);
            }
        }

        /* access modifiers changed from: package-private */
        public Set<Inet6Address> getDnses() {
            return this.mDnses.keySet();
        }

        /* access modifiers changed from: package-private */
        public void putDnses(Set<Inet6Address> dnses) {
            for (Inet6Address dns : dnses) {
                this.mDnses.put(dns, 5);
            }
        }

        /* access modifiers changed from: package-private */
        public void removeDnses(Set<Inet6Address> dnses) {
            for (Inet6Address dns : dnses) {
                this.mDnses.remove(dns);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isEmpty() {
            return this.mPrefixes.isEmpty() && this.mDnses.isEmpty();
        }

        /* access modifiers changed from: private */
        public boolean decrementCounters() {
            return decrementCounter(this.mPrefixes) | decrementCounter(this.mDnses);
        }

        private <T> boolean decrementCounter(HashMap<T, Integer> map) {
            boolean removed = false;
            Iterator<Map.Entry<T, Integer>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<T, Integer> kv = it.next();
                if (kv.getValue().intValue() == 0) {
                    it.remove();
                    removed = true;
                } else {
                    kv.setValue(Integer.valueOf(kv.getValue().intValue() - 1));
                }
            }
            return removed;
        }
    }

    private final class MulticastTransmitter extends Thread {
        private final Random mRandom;
        private final AtomicInteger mUrgentAnnouncements;

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

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x002b, code lost:
            if (r4.mUrgentAnnouncements.getAndDecrement() > 0) goto L_0x003a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x002d, code lost:
            if (r0 == false) goto L_0x0030;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0039, code lost:
            return 300 + r4.mRandom.nextInt(300);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x003b, code lost:
            return 3;
         */
        private int getNextMulticastTransmitDelaySec() {
            synchronized (RouterAdvertisementDaemon.this.mLock) {
                if (RouterAdvertisementDaemon.this.mRaLength < 16) {
                    return RouterAdvertisementDaemon.DAY_IN_SECONDS;
                }
                boolean deprecationInProgress = !RouterAdvertisementDaemon.this.mDeprecatedInfoTracker.isEmpty();
            }
        }

        private long getNextMulticastTransmitDelayMs() {
            return 1000 * ((long) getNextMulticastTransmitDelaySec());
        }
    }

    public static class RaParams {
        public HashSet<Inet6Address> dnses;
        public boolean hasDefaultRoute;
        public int mtu;
        public HashSet<IpPrefix> prefixes;

        public RaParams() {
            this.hasDefaultRoute = false;
            this.mtu = 1280;
            this.prefixes = new HashSet<>();
            this.dnses = new HashSet<>();
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
                Iterator<IpPrefix> it = oldRa.prefixes.iterator();
                while (it.hasNext()) {
                    IpPrefix ipp = it.next();
                    if (newRa == null || !newRa.prefixes.contains(ipp)) {
                        newlyDeprecated.prefixes.add(ipp);
                    }
                }
                Iterator<Inet6Address> it2 = oldRa.dnses.iterator();
                while (it2.hasNext()) {
                    Inet6Address dns = it2.next();
                    if (newRa == null || !newRa.dnses.contains(dns)) {
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

        private UnicastResponder() {
            this.solicitor = new InetSocketAddress();
            this.mSolication = new byte[1280];
        }

        public void run() {
            while (RouterAdvertisementDaemon.this.isSocketValid()) {
                try {
                    if (Os.recvfrom(RouterAdvertisementDaemon.this.mSocket, this.mSolication, 0, this.mSolication.length, 0, this.solicitor) >= 1 && this.mSolication[0] == RouterAdvertisementDaemon.ICMPV6_ND_ROUTER_SOLICIT) {
                        RouterAdvertisementDaemon.this.maybeSendRA(this.solicitor);
                    }
                } catch (ErrnoException | SocketException e) {
                    if (RouterAdvertisementDaemon.this.isSocketValid()) {
                        String access$600 = RouterAdvertisementDaemon.TAG;
                        Log.e(access$600, "recvfrom error: " + e);
                    }
                }
            }
        }
    }

    public RouterAdvertisementDaemon(InterfaceParams ifParams) {
        this.mInterface = ifParams;
        this.mAllNodes = new InetSocketAddress(getAllNodesForScopeId(this.mInterface.index), 0);
        this.mDeprecatedInfoTracker = new DeprecatedInfoTracker();
    }

    public void buildNewRa(RaParams deprecatedParams, RaParams newParams) {
        synchronized (this.mLock) {
            if (deprecatedParams != null) {
                try {
                    this.mDeprecatedInfoTracker.putPrefixes(deprecatedParams.prefixes);
                    this.mDeprecatedInfoTracker.putDnses(deprecatedParams.dnses);
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
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
        this.mMulticastTransmitter = new MulticastTransmitter();
        this.mMulticastTransmitter.start();
        this.mUnicastResponder = new UnicastResponder();
        this.mUnicastResponder.start();
        return true;
    }

    public void stop() {
        closeSocket();
        this.mMulticastTransmitter = null;
        this.mUnicastResponder = null;
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public void assembleRaLocked() {
        ByteBuffer ra = ByteBuffer.wrap(this.mRA);
        ra.order(ByteOrder.BIG_ENDIAN);
        boolean shouldSendRA = false;
        try {
            putHeader(ra, this.mRaParams != null && this.mRaParams.hasDefaultRoute);
            putSlla(ra, this.mInterface.macAddr.toByteArray());
            this.mRaLength = ra.position();
            if (this.mRaParams != null) {
                putMtu(ra, this.mRaParams.mtu);
                this.mRaLength = ra.position();
                Iterator<IpPrefix> it = this.mRaParams.prefixes.iterator();
                while (it.hasNext()) {
                    putPio(ra, it.next(), DEFAULT_LIFETIME, DEFAULT_LIFETIME);
                    this.mRaLength = ra.position();
                    shouldSendRA = true;
                }
                if (this.mRaParams.dnses.size() > 0) {
                    putRdnss(ra, this.mRaParams.dnses, DEFAULT_LIFETIME);
                    this.mRaLength = ra.position();
                    shouldSendRA = true;
                }
            }
            for (IpPrefix ipp : this.mDeprecatedInfoTracker.getPrefixes()) {
                putPio(ra, ipp, 0, 0);
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
            String str = TAG;
            Log.wtf(str, "Failed to construct ff02::1 InetAddress: " + uhe);
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
        ra.put(ICMPV6_ND_ROUTER_ADVERT).put(asByte(0)).putShort(asShort(0)).put((byte) 64).put(hasDefaultRoute ? asByte(8) : asByte(0)).putShort(hasDefaultRoute ? asShort(DEFAULT_LIFETIME) : asShort(0)).putInt(0).putInt(0);
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
        int i = 1280;
        if (mtu >= 1280) {
            i = mtu;
        }
        putShort.putInt(i);
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
            int i = 8;
            byte RIO_NUM_8OCTETS = asByte(prefixLength == 0 ? 1 : prefixLength <= 8 ? 2 : 3);
            byte[] addr = ipp.getAddress().getAddress();
            ra.put((byte) 24).put(RIO_NUM_8OCTETS).put(asByte(prefixLength)).put(asByte(24)).putInt(DEFAULT_LIFETIME);
            if (prefixLength > 0) {
                if (prefixLength > 64) {
                    i = 16;
                }
                ra.put(addr, 0, i);
            }
        }
    }

    private static void putRdnss(ByteBuffer ra, Set<Inet6Address> dnses, int lifetime) {
        HashSet<Inet6Address> filteredDnses = new HashSet<>();
        for (Inet6Address dns : dnses) {
            if (new LinkAddress(dns, 64).isGlobalPreferred()) {
                filteredDnses.add(dns);
            }
        }
        if (!filteredDnses.isEmpty()) {
            ra.put((byte) 25).put(asByte((dnses.size() * 2) + 1)).putShort(asShort(0)).putInt(lifetime);
            Iterator<Inet6Address> it = filteredDnses.iterator();
            while (it.hasNext()) {
                ra.put(it.next().getAddress());
            }
        }
    }

    private boolean createSocket() {
        int oldTag = TrafficStats.getAndSetThreadStatsTag(-189);
        try {
            this.mSocket = Os.socket(OsConstants.AF_INET6, OsConstants.SOCK_RAW, OsConstants.IPPROTO_ICMPV6);
            Os.setsockoptTimeval(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(300));
            Os.setsockoptIfreq(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_BINDTODEVICE, this.mInterface.name);
            NetworkUtils.protectFromVpn(this.mSocket);
            NetworkUtils.setupRaSocket(this.mSocket, this.mInterface.index);
            TrafficStats.setThreadStatsTag(oldTag);
            return true;
        } catch (ErrnoException | IOException e) {
            String str = TAG;
            Log.e(str, "Failed to create RA daemon socket: " + e);
            TrafficStats.setThreadStatsTag(oldTag);
            return false;
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

    /* access modifiers changed from: private */
    public boolean isSocketValid() {
        FileDescriptor s = this.mSocket;
        return s != null && s.valid();
    }

    private boolean isSuitableDestination(InetSocketAddress dest) {
        boolean z = true;
        if (this.mAllNodes.equals(dest)) {
            return true;
        }
        InetAddress destip = dest.getAddress();
        if (!(destip instanceof Inet6Address) || !destip.isLinkLocalAddress() || ((Inet6Address) destip).getScopeId() != this.mInterface.index) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r0 = TAG;
        android.util.Log.d(r0, "RA sendto " + r8.getAddress().getHostAddress());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0062, code lost:
        return;
     */
    public void maybeSendRA(InetSocketAddress dest) {
        if (dest == null || !isSuitableDestination(dest)) {
            dest = this.mAllNodes;
        }
        try {
            synchronized (this.mLock) {
                if (this.mRaLength >= 16) {
                    Os.sendto(this.mSocket, this.mRA, 0, this.mRaLength, 0, dest);
                }
            }
        } catch (ErrnoException | SocketException e) {
            if (isSocketValid()) {
                String str = TAG;
                Log.e(str, "sendto error: " + e);
            }
        }
    }
}
