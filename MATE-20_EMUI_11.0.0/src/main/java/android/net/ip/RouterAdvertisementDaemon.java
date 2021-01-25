package android.net.ip;

import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.TrafficStats;
import android.net.util.InterfaceParams;
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
    private static final byte ICMPV6_ND_ROUTER_ADVERT = asByte(134);
    private static final byte ICMPV6_ND_ROUTER_SOLICIT = asByte(133);
    private static final int MAX_RTR_ADV_INTERVAL_SEC = 600;
    private static final int MAX_URGENT_RTR_ADVERTISEMENTS = 5;
    private static final int MIN_DELAY_BETWEEN_RAS_SEC = 3;
    private static final int MIN_RA_HEADER_SIZE = 16;
    private static final int MIN_RTR_ADV_INTERVAL_SEC = 300;
    private static final String TAG = RouterAdvertisementDaemon.class.getSimpleName();
    private final InetSocketAddress mAllNodes;
    @GuardedBy({"mLock"})
    private final DeprecatedInfoTracker mDeprecatedInfoTracker;
    private final InterfaceParams mInterface;
    private final Object mLock = new Object();
    private volatile MulticastTransmitter mMulticastTransmitter;
    @GuardedBy({"mLock"})
    private final byte[] mRA = new byte[1280];
    @GuardedBy({"mLock"})
    private int mRaLength;
    @GuardedBy({"mLock"})
    private RaParams mRaParams;
    private volatile FileDescriptor mSocket;
    private volatile UnicastResponder mUnicastResponder;

    public static class RaParams {
        static final byte DEFAULT_HOPLIMIT = 65;
        public HashSet<Inet6Address> dnses;
        public boolean hasDefaultRoute;
        public byte hopLimit;
        public int mtu;
        public HashSet<IpPrefix> prefixes;

        public RaParams() {
            this.hasDefaultRoute = false;
            this.hopLimit = DEFAULT_HOPLIMIT;
            this.mtu = 1280;
            this.prefixes = new HashSet<>();
            this.dnses = new HashSet<>();
        }

        public RaParams(RaParams other) {
            this.hasDefaultRoute = other.hasDefaultRoute;
            this.hopLimit = other.hopLimit;
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

    /* access modifiers changed from: private */
    public static class DeprecatedInfoTracker {
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
        /* access modifiers changed from: public */
        private boolean decrementCounters() {
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
                    throw th;
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
        maybeNotifyMulticastTransmitter();
        this.mMulticastTransmitter = null;
        this.mUnicastResponder = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0024 A[Catch:{ BufferOverflowException -> 0x001e }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0029 A[Catch:{ BufferOverflowException -> 0x001e }] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0041 A[Catch:{ BufferOverflowException -> 0x001e }] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0098 A[Catch:{ BufferOverflowException -> 0x001e }, LOOP:1: B:26:0x0092->B:28:0x0098, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00b5 A[Catch:{ BufferOverflowException -> 0x001e }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00d8  */
    /* JADX WARNING: Removed duplicated region for block: B:39:? A[RETURN, SYNTHETIC] */
    @GuardedBy({"mLock"})
    private void assembleRaLocked() {
        Set<Inet6Address> deprecatedDnses;
        ByteBuffer ra = ByteBuffer.wrap(this.mRA);
        ra.order(ByteOrder.BIG_ENDIAN);
        boolean z = true;
        boolean haveRaParams = this.mRaParams != null;
        boolean shouldSendRA = false;
        if (haveRaParams) {
            try {
                if (this.mRaParams.hasDefaultRoute) {
                    putHeader(ra, z, !haveRaParams ? this.mRaParams.hopLimit : 65);
                    putSlla(ra, this.mInterface.macAddr.toByteArray());
                    this.mRaLength = ra.position();
                    if (haveRaParams) {
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
                    deprecatedDnses = this.mDeprecatedInfoTracker.getDnses();
                    if (!deprecatedDnses.isEmpty()) {
                        putRdnss(ra, deprecatedDnses, 0);
                        this.mRaLength = ra.position();
                        shouldSendRA = true;
                    }
                    if (shouldSendRA) {
                        this.mRaLength = 0;
                        return;
                    }
                    return;
                }
            } catch (BufferOverflowException e) {
                Log.e(TAG, "Could not construct new RA: " + e);
            }
        }
        z = false;
        putHeader(ra, z, !haveRaParams ? this.mRaParams.hopLimit : 65);
        putSlla(ra, this.mInterface.macAddr.toByteArray());
        this.mRaLength = ra.position();
        if (haveRaParams) {
        }
        while (r2.hasNext()) {
        }
        deprecatedDnses = this.mDeprecatedInfoTracker.getDnses();
        if (!deprecatedDnses.isEmpty()) {
        }
        if (shouldSendRA) {
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

    private static void putHeader(ByteBuffer ra, boolean hasDefaultRoute, byte hopLimit) {
        ra.put(ICMPV6_ND_ROUTER_ADVERT).put(asByte(0)).putShort(asShort(0)).put(hopLimit).put(hasDefaultRoute ? asByte(8) : asByte(0)).putShort(hasDefaultRoute ? asShort(DEFAULT_LIFETIME) : asShort(0)).putInt(0).putInt(0);
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
        int oldTag = TrafficStats.getAndSetThreadStatsTag(-510);
        try {
            this.mSocket = Os.socket(OsConstants.AF_INET6, OsConstants.SOCK_RAW, OsConstants.IPPROTO_ICMPV6);
            Os.setsockoptTimeval(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(300));
            Os.setsockoptIfreq(this.mSocket, OsConstants.SOL_SOCKET, OsConstants.SO_BINDTODEVICE, this.mInterface.name);
            NetworkUtils.protectFromVpn(this.mSocket);
            NetworkUtils.setupRaSocket(this.mSocket, this.mInterface.index);
            TrafficStats.setThreadStatsTag(oldTag);
            return true;
        } catch (ErrnoException | IOException e) {
            Log.e(TAG, "Failed to create RA daemon socket.");
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
    /* access modifiers changed from: public */
    private boolean isSocketValid() {
        FileDescriptor s = this.mSocket;
        return s != null && s.valid();
    }

    private boolean isSuitableDestination(InetSocketAddress dest) {
        if (this.mAllNodes.equals(dest)) {
            return true;
        }
        InetAddress destip = dest.getAddress();
        if (!(destip instanceof Inet6Address) || !destip.isLinkLocalAddress() || ((Inet6Address) destip).getScopeId() != this.mInterface.index) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeSendRA(InetSocketAddress dest) {
        if (dest == null || !isSuitableDestination(dest)) {
            dest = this.mAllNodes;
        }
        synchronized (this.mLock) {
            if (this.mRaLength >= 16) {
                Os.sendto(this.mSocket, this.mRA, 0, this.mRaLength, 0, dest);
                try {
                    Log.d(TAG, "RA sendto.");
                } catch (ErrnoException | SocketException e) {
                    if (isSocketValid()) {
                        Log.e(TAG, "sendto error.");
                    }
                }
            }
        }
    }

    private final class UnicastResponder extends Thread {
        private final byte[] mSolication;
        private final InetSocketAddress solicitor;

        private UnicastResponder() {
            this.solicitor = new InetSocketAddress();
            this.mSolication = new byte[1280];
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (RouterAdvertisementDaemon.this.isSocketValid()) {
                try {
                    if (Os.recvfrom(RouterAdvertisementDaemon.this.mSocket, this.mSolication, 0, this.mSolication.length, 0, this.solicitor) >= 1 && this.mSolication[0] == RouterAdvertisementDaemon.ICMPV6_ND_ROUTER_SOLICIT) {
                        RouterAdvertisementDaemon.this.maybeSendRA(this.solicitor);
                    }
                } catch (ErrnoException | SocketException e) {
                    if (RouterAdvertisementDaemon.this.isSocketValid()) {
                        String str = RouterAdvertisementDaemon.TAG;
                        Log.e(str, "recvfrom error: " + e);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class MulticastTransmitter extends Thread {
        private final Random mRandom;
        private final AtomicInteger mUrgentAnnouncements;

        private MulticastTransmitter() {
            this.mRandom = new Random();
            this.mUrgentAnnouncements = new AtomicInteger(0);
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (RouterAdvertisementDaemon.this.isSocketValid()) {
                try {
                    Thread.sleep(getNextMulticastTransmitDelayMs());
                } catch (InterruptedException e) {
                }
                RouterAdvertisementDaemon routerAdvertisementDaemon = RouterAdvertisementDaemon.this;
                routerAdvertisementDaemon.maybeSendRA(routerAdvertisementDaemon.mAllNodes);
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

        private int getNextMulticastTransmitDelaySec() {
            boolean deprecationInProgress;
            synchronized (RouterAdvertisementDaemon.this.mLock) {
                if (RouterAdvertisementDaemon.this.mRaLength < 16) {
                    return RouterAdvertisementDaemon.DAY_IN_SECONDS;
                }
                deprecationInProgress = !RouterAdvertisementDaemon.this.mDeprecatedInfoTracker.isEmpty();
            }
            if (this.mUrgentAnnouncements.getAndDecrement() > 0 || deprecationInProgress) {
                return 3;
            }
            return this.mRandom.nextInt(300) + 300;
        }

        private long getNextMulticastTransmitDelayMs() {
            return ((long) getNextMulticastTransmitDelaySec()) * 1000;
        }
    }
}
