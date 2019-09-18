package com.android.server.connectivity.tethering;

import android.content.ContentResolver;
import android.net.ITetheringStatsProvider;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkStats;
import android.net.RouteInfo;
import android.net.netlink.ConntrackMessage;
import android.net.netlink.NetlinkConstants;
import android.net.netlink.NetlinkSocket;
import android.net.util.IpUtils;
import android.net.util.SharedLog;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.text.TextUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.connectivity.tethering.OffloadController;
import com.android.server.connectivity.tethering.OffloadHardwareInterface;
import com.android.server.job.controllers.JobStatus;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OffloadController {
    private static final String ANYIP = "0.0.0.0";
    private static final boolean DBG = false;
    private static final OffloadHardwareInterface.ForwardedStats EMPTY_STATS = new OffloadHardwareInterface.ForwardedStats();
    private static final String TAG = OffloadController.class.getSimpleName();
    private boolean mConfigInitialized;
    private final ContentResolver mContentResolver;
    private boolean mControlInitialized;
    private final HashMap<String, LinkProperties> mDownstreams;
    private Set<IpPrefix> mExemptPrefixes;
    /* access modifiers changed from: private */
    public ConcurrentHashMap<String, OffloadHardwareInterface.ForwardedStats> mForwardedStats = new ConcurrentHashMap<>(16, 0.75f, 1);
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final OffloadHardwareInterface mHwInterface;
    /* access modifiers changed from: private */
    public HashMap<String, Long> mInterfaceQuotas = new HashMap<>();
    private Set<String> mLastLocalPrefixStrs;
    /* access modifiers changed from: private */
    public final SharedLog mLog;
    private int mNatUpdateCallbacksReceived;
    private int mNatUpdateNetlinkErrors;
    private final INetworkManagementService mNms;
    private final ITetheringStatsProvider mStatsProvider;
    private LinkProperties mUpstreamLinkProperties;

    private class OffloadTetheringStatsProvider extends ITetheringStatsProvider.Stub {
        private OffloadTetheringStatsProvider() {
        }

        public NetworkStats getTetherStats(int how) {
            Runnable updateStats = new Runnable() {
                public final void run() {
                    OffloadController.this.updateStatsForCurrentUpstream();
                }
            };
            if (Looper.myLooper() == OffloadController.this.mHandler.getLooper()) {
                updateStats.run();
            } else {
                OffloadController.this.mHandler.post(updateStats);
            }
            NetworkStats stats = new NetworkStats(SystemClock.elapsedRealtime(), 0);
            NetworkStats.Entry entry = new NetworkStats.Entry();
            entry.set = 0;
            entry.tag = 0;
            entry.uid = how == 1 ? -5 : -1;
            for (Map.Entry<String, OffloadHardwareInterface.ForwardedStats> kv : OffloadController.this.mForwardedStats.entrySet()) {
                OffloadHardwareInterface.ForwardedStats value = kv.getValue();
                entry.iface = kv.getKey();
                entry.rxBytes = value.rxBytes;
                entry.txBytes = value.txBytes;
                stats.addValues(entry);
            }
            return stats;
        }

        public void setInterfaceQuota(String iface, long quotaBytes) {
            OffloadController.this.mHandler.post(new Runnable(quotaBytes, iface) {
                private final /* synthetic */ long f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                }

                public final void run() {
                    OffloadController.OffloadTetheringStatsProvider.lambda$setInterfaceQuota$1(OffloadController.OffloadTetheringStatsProvider.this, this.f$1, this.f$2);
                }
            });
        }

        public static /* synthetic */ void lambda$setInterfaceQuota$1(OffloadTetheringStatsProvider offloadTetheringStatsProvider, long quotaBytes, String iface) {
            if (quotaBytes == -1) {
                OffloadController.this.mInterfaceQuotas.remove(iface);
            } else {
                OffloadController.this.mInterfaceQuotas.put(iface, Long.valueOf(quotaBytes));
            }
            boolean unused = OffloadController.this.maybeUpdateDataLimit(iface);
        }
    }

    private enum UpdateType {
        IF_NEEDED,
        FORCE
    }

    public OffloadController(Handler h, OffloadHardwareInterface hwi, ContentResolver contentResolver, INetworkManagementService nms, SharedLog log) {
        this.mHandler = h;
        this.mHwInterface = hwi;
        this.mContentResolver = contentResolver;
        this.mNms = nms;
        this.mStatsProvider = new OffloadTetheringStatsProvider();
        this.mLog = log.forSubComponent(TAG);
        this.mDownstreams = new HashMap<>();
        this.mExemptPrefixes = new HashSet();
        this.mLastLocalPrefixStrs = new HashSet();
        try {
            this.mNms.registerTetheringStatsProvider(this.mStatsProvider, getClass().getSimpleName());
        } catch (RemoteException e) {
            SharedLog sharedLog = this.mLog;
            sharedLog.e("Cannot register offload stats provider: " + e);
        }
    }

    public boolean start() {
        if (started()) {
            return true;
        }
        if (isOffloadDisabled()) {
            this.mLog.i("tethering offload disabled");
            return false;
        }
        if (!this.mConfigInitialized) {
            this.mConfigInitialized = this.mHwInterface.initOffloadConfig();
            if (!this.mConfigInitialized) {
                this.mLog.i("tethering offload config not supported");
                stop();
                return false;
            }
        }
        this.mControlInitialized = this.mHwInterface.initOffloadControl(new OffloadHardwareInterface.ControlCallback() {
            public void onStarted() {
                if (OffloadController.this.started()) {
                    OffloadController.this.mLog.log("onStarted");
                }
            }

            public void onStoppedError() {
                if (OffloadController.this.started()) {
                    OffloadController.this.mLog.log("onStoppedError");
                }
            }

            public void onStoppedUnsupported() {
                if (OffloadController.this.started()) {
                    OffloadController.this.mLog.log("onStoppedUnsupported");
                    OffloadController.this.updateStatsForAllUpstreams();
                    OffloadController.this.forceTetherStatsPoll();
                }
            }

            public void onSupportAvailable() {
                if (OffloadController.this.started()) {
                    OffloadController.this.mLog.log("onSupportAvailable");
                    OffloadController.this.updateStatsForAllUpstreams();
                    OffloadController.this.forceTetherStatsPoll();
                    boolean unused = OffloadController.this.computeAndPushLocalPrefixes(UpdateType.FORCE);
                    OffloadController.this.pushAllDownstreamState();
                    boolean unused2 = OffloadController.this.pushUpstreamParameters(null);
                }
            }

            public void onStoppedLimitReached() {
                if (OffloadController.this.started()) {
                    OffloadController.this.mLog.log("onStoppedLimitReached");
                    OffloadController.this.updateStatsForCurrentUpstream();
                    OffloadController.this.forceTetherStatsPoll();
                }
            }

            public void onNatTimeoutUpdate(int proto, String srcAddr, int srcPort, String dstAddr, int dstPort) {
                if (OffloadController.this.started()) {
                    OffloadController.this.updateNatTimeout(proto, srcAddr, srcPort, dstAddr, dstPort);
                }
            }
        });
        boolean isStarted = started();
        if (!isStarted) {
            this.mLog.i("tethering offload control not supported");
            stop();
        } else {
            this.mLog.log("tethering offload started");
            this.mNatUpdateCallbacksReceived = 0;
            this.mNatUpdateNetlinkErrors = 0;
        }
        return isStarted;
    }

    public void stop() {
        boolean wasStarted = started();
        updateStatsForCurrentUpstream();
        this.mUpstreamLinkProperties = null;
        this.mHwInterface.stopOffloadControl();
        this.mControlInitialized = false;
        this.mConfigInitialized = false;
        if (wasStarted) {
            this.mLog.log("tethering offload stopped");
        }
    }

    /* access modifiers changed from: private */
    public boolean started() {
        return this.mConfigInitialized && this.mControlInitialized;
    }

    private String currentUpstreamInterface() {
        if (this.mUpstreamLinkProperties != null) {
            return this.mUpstreamLinkProperties.getInterfaceName();
        }
        return null;
    }

    private void maybeUpdateStats(String iface) {
        if (!TextUtils.isEmpty(iface)) {
            OffloadHardwareInterface.ForwardedStats diff = this.mHwInterface.getForwardedStats(iface);
            OffloadHardwareInterface.ForwardedStats base = this.mForwardedStats.get(iface);
            if (base != null) {
                diff.add(base);
            }
            this.mForwardedStats.put(iface, diff);
        }
    }

    /* access modifiers changed from: private */
    public boolean maybeUpdateDataLimit(String iface) {
        if (!started() || !TextUtils.equals(iface, currentUpstreamInterface())) {
            return true;
        }
        Long limit = this.mInterfaceQuotas.get(iface);
        if (limit == null) {
            limit = Long.valueOf(JobStatus.NO_LATEST_RUNTIME);
        }
        return this.mHwInterface.setDataLimit(iface, limit.longValue());
    }

    /* access modifiers changed from: private */
    public void updateStatsForCurrentUpstream() {
        maybeUpdateStats(currentUpstreamInterface());
    }

    /* access modifiers changed from: private */
    public void updateStatsForAllUpstreams() {
        for (Map.Entry<String, OffloadHardwareInterface.ForwardedStats> kv : this.mForwardedStats.entrySet()) {
            maybeUpdateStats(kv.getKey());
        }
    }

    /* access modifiers changed from: private */
    public void forceTetherStatsPoll() {
        try {
            this.mNms.tetherLimitReached(this.mStatsProvider);
        } catch (RemoteException e) {
            SharedLog sharedLog = this.mLog;
            sharedLog.e("Cannot report data limit reached: " + e);
        }
    }

    public void setUpstreamLinkProperties(LinkProperties lp) {
        if (started() && !Objects.equals(this.mUpstreamLinkProperties, lp)) {
            String prevUpstream = currentUpstreamInterface();
            this.mUpstreamLinkProperties = lp != null ? new LinkProperties(lp) : null;
            String iface = currentUpstreamInterface();
            if (!TextUtils.isEmpty(iface)) {
                this.mForwardedStats.putIfAbsent(iface, EMPTY_STATS);
            }
            computeAndPushLocalPrefixes(UpdateType.IF_NEEDED);
            pushUpstreamParameters(prevUpstream);
        }
    }

    public void setLocalPrefixes(Set<IpPrefix> localPrefixes) {
        this.mExemptPrefixes = localPrefixes;
        if (started()) {
            computeAndPushLocalPrefixes(UpdateType.IF_NEEDED);
        }
    }

    public void notifyDownstreamLinkProperties(LinkProperties lp) {
        LinkProperties oldLp = this.mDownstreams.put(lp.getInterfaceName(), new LinkProperties(lp));
        if (!Objects.equals(oldLp, lp) && started()) {
            pushDownstreamState(oldLp, lp);
        }
    }

    private void pushDownstreamState(LinkProperties oldLp, LinkProperties newLp) {
        String ifname = newLp.getInterfaceName();
        List<RouteInfo> oldRoutes = oldLp != null ? oldLp.getRoutes() : Collections.EMPTY_LIST;
        List<RouteInfo> newRoutes = newLp.getRoutes();
        for (RouteInfo ri : oldRoutes) {
            if (!shouldIgnoreDownstreamRoute(ri) && !newRoutes.contains(ri)) {
                this.mHwInterface.removeDownstreamPrefix(ifname, ri.getDestination().toString());
            }
        }
        for (RouteInfo ri2 : newRoutes) {
            if (!shouldIgnoreDownstreamRoute(ri2) && !oldRoutes.contains(ri2)) {
                this.mHwInterface.addDownstreamPrefix(ifname, ri2.getDestination().toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public void pushAllDownstreamState() {
        for (LinkProperties lp : this.mDownstreams.values()) {
            pushDownstreamState(null, lp);
        }
    }

    public void removeDownstreamInterface(String ifname) {
        LinkProperties lp = this.mDownstreams.remove(ifname);
        if (lp != null && started()) {
            for (RouteInfo route : lp.getRoutes()) {
                if (!shouldIgnoreDownstreamRoute(route)) {
                    this.mHwInterface.removeDownstreamPrefix(ifname, route.getDestination().toString());
                }
            }
        }
    }

    private boolean isOffloadDisabled() {
        return Settings.Global.getInt(this.mContentResolver, "tether_offload_disabled", this.mHwInterface.getDefaultTetherOffloadDisabled()) != 0;
    }

    /* access modifiers changed from: private */
    public boolean pushUpstreamParameters(String prevUpstream) {
        String iface = currentUpstreamInterface();
        ArrayList<String> arrayList = null;
        if (TextUtils.isEmpty(iface)) {
            boolean rval = this.mHwInterface.setUpstreamParameters(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, ANYIP, ANYIP, null);
            maybeUpdateStats(prevUpstream);
            return rval;
        }
        ArrayList<String> v6gateways = new ArrayList<>();
        String v4addr = null;
        String v4gateway = null;
        Iterator it = this.mUpstreamLinkProperties.getAddresses().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            InetAddress ip = (InetAddress) it.next();
            if (ip instanceof Inet4Address) {
                v4addr = ip.getHostAddress();
                break;
            }
        }
        for (RouteInfo ri : this.mUpstreamLinkProperties.getRoutes()) {
            if (ri.hasGateway()) {
                String gateway = ri.getGateway().getHostAddress();
                if (ri.isIPv4Default()) {
                    v4gateway = gateway;
                } else if (ri.isIPv6Default()) {
                    v6gateways.add(gateway);
                }
            }
        }
        OffloadHardwareInterface offloadHardwareInterface = this.mHwInterface;
        if (!v6gateways.isEmpty()) {
            arrayList = v6gateways;
        }
        boolean success = offloadHardwareInterface.setUpstreamParameters(iface, v4addr, v4gateway, arrayList);
        if (!success) {
            return success;
        }
        maybeUpdateStats(prevUpstream);
        boolean success2 = maybeUpdateDataLimit(iface);
        if (!success2) {
            SharedLog sharedLog = this.mLog;
            sharedLog.log("Setting data limit for " + iface + " failed, disabling offload.");
            stop();
        }
        return success2;
    }

    /* access modifiers changed from: private */
    public boolean computeAndPushLocalPrefixes(UpdateType how) {
        boolean force = how == UpdateType.FORCE;
        Set<String> localPrefixStrs = computeLocalPrefixStrings(this.mExemptPrefixes, this.mUpstreamLinkProperties);
        if (!force && this.mLastLocalPrefixStrs.equals(localPrefixStrs)) {
            return true;
        }
        this.mLastLocalPrefixStrs = localPrefixStrs;
        return this.mHwInterface.setLocalPrefixes(new ArrayList(localPrefixStrs));
    }

    private static Set<String> computeLocalPrefixStrings(Set<IpPrefix> localPrefixes, LinkProperties upstreamLinkProperties) {
        Set<IpPrefix> prefixSet = new HashSet<>(localPrefixes);
        if (upstreamLinkProperties != null) {
            for (LinkAddress linkAddr : upstreamLinkProperties.getLinkAddresses()) {
                if (linkAddr.isGlobalPreferred()) {
                    InetAddress ip = linkAddr.getAddress();
                    if (ip instanceof Inet6Address) {
                        prefixSet.add(new IpPrefix(ip, 128));
                    }
                }
            }
        }
        HashSet<String> localPrefixStrs = new HashSet<>();
        for (IpPrefix pfx : prefixSet) {
            localPrefixStrs.add(pfx.toString());
        }
        return localPrefixStrs;
    }

    private static boolean shouldIgnoreDownstreamRoute(RouteInfo route) {
        if (!route.getDestinationLinkAddress().isGlobalPreferred()) {
            return true;
        }
        return false;
    }

    public void dump(IndentingPrintWriter pw) {
        if (isOffloadDisabled()) {
            pw.println("Offload disabled");
            return;
        }
        boolean isStarted = started();
        StringBuilder sb = new StringBuilder();
        sb.append("Offload HALs ");
        sb.append(isStarted ? "started" : "not started");
        pw.println(sb.toString());
        LinkProperties lp = this.mUpstreamLinkProperties;
        String upstream = lp != null ? lp.getInterfaceName() : null;
        pw.println("Current upstream: " + upstream);
        pw.println("Exempt prefixes: " + this.mLastLocalPrefixStrs);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("NAT timeout update callbacks received during the ");
        sb2.append(isStarted ? "current" : "last");
        sb2.append(" offload session: ");
        sb2.append(this.mNatUpdateCallbacksReceived);
        pw.println(sb2.toString());
        StringBuilder sb3 = new StringBuilder();
        sb3.append("NAT timeout update netlink errors during the ");
        sb3.append(isStarted ? "current" : "last");
        sb3.append(" offload session: ");
        sb3.append(this.mNatUpdateNetlinkErrors);
        pw.println(sb3.toString());
    }

    /* access modifiers changed from: private */
    public void updateNatTimeout(int proto, String srcAddr, int srcPort, String dstAddr, int dstPort) {
        String str = srcAddr;
        String str2 = dstAddr;
        String protoName = protoNameFor(proto);
        if (protoName == null) {
            this.mLog.e("Unknown NAT update callback protocol: " + proto);
            return;
        }
        int i = proto;
        Inet4Address src = parseIPv4Address(srcAddr);
        if (src == null) {
            this.mLog.e("Failed to parse IPv4 address: " + str);
        } else if (!IpUtils.isValidUdpOrTcpPort(srcPort)) {
            this.mLog.e("Invalid src port: " + srcPort);
        } else {
            int i2 = srcPort;
            Inet4Address dst = parseIPv4Address(dstAddr);
            if (dst == null) {
                this.mLog.e("Failed to parse IPv4 address: " + str2);
            } else if (!IpUtils.isValidUdpOrTcpPort(dstPort)) {
                this.mLog.e("Invalid dst port: " + dstPort);
            } else {
                this.mNatUpdateCallbacksReceived++;
                String natDescription = String.format("%s (%s, %s) -> (%s, %s)", new Object[]{protoName, str, Integer.valueOf(srcPort), str2, Integer.valueOf(dstPort)});
                try {
                    NetlinkSocket.sendOneShotKernelMessage(OsConstants.NETLINK_NETFILTER, ConntrackMessage.newIPv4TimeoutUpdateRequest(i, src, i2, dst, dstPort, connectionTimeoutUpdateSecondsFor(proto)));
                } catch (ErrnoException e) {
                    this.mNatUpdateNetlinkErrors++;
                    this.mLog.e("Error updating NAT conntrack entry >" + natDescription + "<: " + e + ", msg: " + NetlinkConstants.hexify(msg));
                    SharedLog sharedLog = this.mLog;
                    StringBuilder sb = new StringBuilder();
                    sb.append("NAT timeout update callbacks received: ");
                    sb.append(this.mNatUpdateCallbacksReceived);
                    sharedLog.log(sb.toString());
                    this.mLog.log("NAT timeout update netlink errors: " + this.mNatUpdateNetlinkErrors);
                }
            }
        }
    }

    private static Inet4Address parseIPv4Address(String addrString) {
        try {
            InetAddress ip = InetAddress.parseNumericAddress(addrString);
            if (ip instanceof Inet4Address) {
                return (Inet4Address) ip;
            }
        } catch (IllegalArgumentException e) {
        }
        return null;
    }

    private static String protoNameFor(int proto) {
        if (proto == OsConstants.IPPROTO_UDP) {
            return "UDP";
        }
        if (proto == OsConstants.IPPROTO_TCP) {
            return "TCP";
        }
        return null;
    }

    private static int connectionTimeoutUpdateSecondsFor(int proto) {
        if (proto == OsConstants.IPPROTO_TCP) {
            return 432000;
        }
        return 180;
    }
}
