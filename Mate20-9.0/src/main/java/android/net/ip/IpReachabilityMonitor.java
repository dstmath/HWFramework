package android.net.ip;

import android.content.Context;
import android.net.LinkProperties;
import android.net.RouteInfo;
import android.net.ip.IpNeighborMonitor;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.IpReachabilityEvent;
import android.net.util.InterfaceParams;
import android.net.util.MultinetworkPolicyTracker;
import android.net.util.SharedLog;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.DumpUtils;
import com.android.server.slice.SliceClientPermissions;
import java.io.PrintWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IpReachabilityMonitor {
    private static final boolean DBG = false;
    private static final String TAG = "IpReachabilityMonitor";
    private static final boolean VDBG = false;
    private final Callback mCallback;
    private final Dependencies mDependencies;
    private final InterfaceParams mInterfaceParams;
    private final IpNeighborMonitor mIpNeighborMonitor;
    private volatile long mLastProbeTimeMs;
    private LinkProperties mLinkProperties;
    private final SharedLog mLog;
    private final IpConnectivityLog mMetricsLog;
    private final MultinetworkPolicyTracker mMultinetworkPolicyTracker;
    private Map<InetAddress, IpNeighborMonitor.NeighborEvent> mNeighborWatchList;

    public interface Callback {
        void notifyLost(InetAddress inetAddress, String str);
    }

    interface Dependencies {
        void acquireWakeLock(long j);

        static Dependencies makeDefault(Context context, String iface) {
            final PowerManager.WakeLock lock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "IpReachabilityMonitor." + iface);
            return new Dependencies() {
                public void acquireWakeLock(long durationMs) {
                    lock.acquire(durationMs);
                }
            };
        }
    }

    public IpReachabilityMonitor(Context context, InterfaceParams ifParams, Handler h, SharedLog log, Callback callback, MultinetworkPolicyTracker tracker) {
        this(ifParams, h, log, callback, tracker, Dependencies.makeDefault(context, ifParams.name));
    }

    @VisibleForTesting
    IpReachabilityMonitor(InterfaceParams ifParams, Handler h, SharedLog log, Callback callback, MultinetworkPolicyTracker tracker, Dependencies dependencies) {
        this.mMetricsLog = new IpConnectivityLog();
        this.mLinkProperties = new LinkProperties();
        this.mNeighborWatchList = new HashMap();
        if (ifParams != null) {
            this.mInterfaceParams = ifParams;
            this.mLog = log.forSubComponent(TAG);
            this.mCallback = callback;
            this.mMultinetworkPolicyTracker = tracker;
            this.mDependencies = dependencies;
            this.mIpNeighborMonitor = new IpNeighborMonitor(h, this.mLog, new IpNeighborMonitor.NeighborEventConsumer() {
                public final void accept(IpNeighborMonitor.NeighborEvent neighborEvent) {
                    IpReachabilityMonitor.lambda$new$0(IpReachabilityMonitor.this, neighborEvent);
                }
            });
            this.mIpNeighborMonitor.start();
            return;
        }
        throw new IllegalArgumentException("null InterfaceParams");
    }

    public static /* synthetic */ void lambda$new$0(IpReachabilityMonitor ipReachabilityMonitor, IpNeighborMonitor.NeighborEvent event) {
        if (ipReachabilityMonitor.mInterfaceParams.index == event.ifindex && ipReachabilityMonitor.mNeighborWatchList.containsKey(event.ip)) {
            IpNeighborMonitor.NeighborEvent prev = ipReachabilityMonitor.mNeighborWatchList.put(event.ip, event);
            if (event.nudState == 32) {
                SharedLog sharedLog = ipReachabilityMonitor.mLog;
                sharedLog.w("ALERT neighbor went from: " + prev + " to: " + event);
                ipReachabilityMonitor.handleNeighborLost(event);
            }
        }
    }

    public void stop() {
        this.mIpNeighborMonitor.stop();
        clearLinkProperties();
    }

    public void dump(PrintWriter pw) {
        DumpUtils.dumpAsync(this.mIpNeighborMonitor.getHandler(), new DumpUtils.Dump() {
            public void dump(PrintWriter pw, String prefix) {
                pw.println(IpReachabilityMonitor.this.describeWatchList("\n"));
            }
        }, pw, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, 1000);
    }

    private String describeWatchList() {
        return describeWatchList(" ");
    }

    /* access modifiers changed from: private */
    public String describeWatchList(String sep) {
        StringBuilder sb = new StringBuilder();
        sb.append("iface{" + this.mInterfaceParams + "}," + sep);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("ntable=[");
        sb2.append(sep);
        sb.append(sb2.toString());
        String delimiter = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        for (Map.Entry<InetAddress, IpNeighborMonitor.NeighborEvent> entry : this.mNeighborWatchList.entrySet()) {
            sb.append(delimiter);
            sb.append(entry.getKey().getHostAddress() + SliceClientPermissions.SliceAuthority.DELIMITER + entry.getValue());
            StringBuilder sb3 = new StringBuilder();
            sb3.append(",");
            sb3.append(sep);
            delimiter = sb3.toString();
        }
        sb.append("]");
        return sb.toString();
    }

    private static boolean isOnLink(List<RouteInfo> routes, InetAddress ip) {
        for (RouteInfo route : routes) {
            if (!route.hasGateway() && route.matches(ip)) {
                return true;
            }
        }
        return false;
    }

    public void updateLinkProperties(LinkProperties lp) {
        if (!this.mInterfaceParams.name.equals(lp.getInterfaceName())) {
            Log.wtf(TAG, "requested LinkProperties interface '" + lp.getInterfaceName() + "' does not match: " + this.mInterfaceParams.name);
            return;
        }
        this.mLinkProperties = new LinkProperties(lp);
        Map<InetAddress, IpNeighborMonitor.NeighborEvent> newNeighborWatchList = new HashMap<>();
        List<RouteInfo> routes = this.mLinkProperties.getRoutes();
        for (RouteInfo route : routes) {
            if (route.hasGateway()) {
                InetAddress gw = route.getGateway();
                if (isOnLink(routes, gw)) {
                    newNeighborWatchList.put(gw, this.mNeighborWatchList.getOrDefault(gw, null));
                }
            }
        }
        for (InetAddress dns : lp.getDnsServers()) {
            if (isOnLink(routes, dns)) {
                newNeighborWatchList.put(dns, this.mNeighborWatchList.getOrDefault(dns, null));
            }
        }
        this.mNeighborWatchList = newNeighborWatchList;
    }

    public void clearLinkProperties() {
        this.mLinkProperties.clear();
        this.mNeighborWatchList.clear();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: java.net.InetAddress} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void handleNeighborLost(IpNeighborMonitor.NeighborEvent event) {
        LinkProperties whatIfLp = new LinkProperties(this.mLinkProperties);
        InetAddress ip = null;
        for (Map.Entry<InetAddress, IpNeighborMonitor.NeighborEvent> entry : this.mNeighborWatchList.entrySet()) {
            if (entry.getValue().nudState == 32) {
                ip = entry.getKey();
                for (RouteInfo route : this.mLinkProperties.getRoutes()) {
                    if (ip.equals(route.getGateway())) {
                        whatIfLp.removeRoute(route);
                    }
                }
                if (avoidingBadLinks() || !(ip instanceof Inet6Address)) {
                    whatIfLp.removeDnsServer(ip);
                }
            }
        }
        LinkProperties.ProvisioningChange delta = LinkProperties.compareProvisioning(this.mLinkProperties, whatIfLp);
        if (delta == LinkProperties.ProvisioningChange.LOST_PROVISIONING) {
            String logMsg = "FAILURE: LOST_PROVISIONING, " + event;
            Log.w(TAG, logMsg);
            if (this.mCallback != null) {
                this.mCallback.notifyLost(ip, logMsg);
            }
        }
        logNudFailed(delta);
    }

    private boolean avoidingBadLinks() {
        return this.mMultinetworkPolicyTracker == null || this.mMultinetworkPolicyTracker.getAvoidBadWifi();
    }

    public void probeAll() {
        List<InetAddress> ipProbeList = new ArrayList<>(this.mNeighborWatchList.keySet());
        if (!ipProbeList.isEmpty()) {
            this.mDependencies.acquireWakeLock(getProbeWakeLockDuration());
        }
        for (InetAddress ip : ipProbeList) {
            int rval = IpNeighborMonitor.startKernelNeighborProbe(this.mInterfaceParams.index, ip);
            this.mLog.log(String.format("put neighbor %s into NUD_PROBE state (rval=%d)", new Object[]{ip.getHostAddress(), Integer.valueOf(rval)}));
            logEvent(256, rval);
        }
        this.mLastProbeTimeMs = SystemClock.elapsedRealtime();
    }

    private static long getProbeWakeLockDuration() {
        return 3500;
    }

    private void logEvent(int probeType, int errorCode) {
        this.mMetricsLog.log(this.mInterfaceParams.name, new IpReachabilityEvent((errorCode & 255) | probeType));
    }

    private void logNudFailed(LinkProperties.ProvisioningChange delta) {
        boolean isProvisioningLost = false;
        boolean isFromProbe = SystemClock.elapsedRealtime() - this.mLastProbeTimeMs < getProbeWakeLockDuration();
        if (delta == LinkProperties.ProvisioningChange.LOST_PROVISIONING) {
            isProvisioningLost = true;
        }
        this.mMetricsLog.log(this.mInterfaceParams.name, new IpReachabilityEvent(IpReachabilityEvent.nudFailureEventType(isFromProbe, isProvisioningLost)));
    }
}
