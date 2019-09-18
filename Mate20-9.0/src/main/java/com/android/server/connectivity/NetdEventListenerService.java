package com.android.server.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetdEventCallback;
import android.net.MacAddress;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.metrics.ConnectStats;
import android.net.metrics.DnsEvent;
import android.net.metrics.INetdEventListener;
import android.net.metrics.NetworkMetrics;
import android.net.metrics.WakeupEvent;
import android.net.metrics.WakeupStats;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.util.StatsLog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.BitUtils;
import com.android.internal.util.RingBuffer;
import com.android.internal.util.TokenBucket;
import com.android.server.HwServiceFactory;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class NetdEventListenerService extends INetdEventListener.Stub {
    @GuardedBy("this")
    private static final int[] ALLOWED_CALLBACK_TYPES = {0, 1, 2};
    private static final int CONNECT_LATENCY_BURST_LIMIT = 5000;
    private static final int CONNECT_LATENCY_FILL_RATE = 15000;
    private static final boolean DBG = false;
    private static final int METRICS_SNAPSHOT_BUFFER_SIZE = 48;
    private static final long METRICS_SNAPSHOT_SPAN_MS = 300000;
    public static final String SERVICE_NAME = "netd_listener";
    private static final String TAG = NetdEventListenerService.class.getSimpleName();
    @VisibleForTesting
    static final int WAKEUP_EVENT_BUFFER_LENGTH = 1024;
    @VisibleForTesting
    static final String WAKEUP_EVENT_IFACE_PREFIX = "iface:";
    private final ConnectivityManager mCm;
    @GuardedBy("this")
    private final TokenBucket mConnectTb;
    private Context mContext;
    @GuardedBy("this")
    private long mLastSnapshot;
    @GuardedBy("this")
    private INetdEventCallback[] mNetdEventCallbackList;
    @GuardedBy("this")
    private final SparseArray<NetworkMetrics> mNetworkMetrics;
    @GuardedBy("this")
    private final RingBuffer<NetworkMetricsSnapshot> mNetworkMetricsSnapshots;
    @GuardedBy("this")
    private final RingBuffer<WakeupEvent> mWakeupEvents;
    @GuardedBy("this")
    private final ArrayMap<String, WakeupStats> mWakeupStats;

    static class NetworkMetricsSnapshot {
        public List<NetworkMetrics.Summary> stats = new ArrayList();
        public long timeMs;

        NetworkMetricsSnapshot() {
        }

        static NetworkMetricsSnapshot collect(long timeMs2, SparseArray<NetworkMetrics> networkMetrics) {
            NetworkMetricsSnapshot snapshot = new NetworkMetricsSnapshot();
            snapshot.timeMs = timeMs2;
            for (int i = 0; i < networkMetrics.size(); i++) {
                NetworkMetrics.Summary s = networkMetrics.valueAt(i).getPendingStats();
                if (s != null) {
                    snapshot.stats.add(s);
                }
            }
            return snapshot;
        }

        public String toString() {
            StringJoiner j = new StringJoiner(", ");
            for (NetworkMetrics.Summary s : this.stats) {
                j.add(s.toString());
            }
            return String.format("%tT.%tL: %s", new Object[]{Long.valueOf(this.timeMs), Long.valueOf(this.timeMs), j.toString()});
        }
    }

    public synchronized boolean addNetdEventCallback(int callerType, INetdEventCallback callback) {
        if (!isValidCallerType(callerType)) {
            String str = TAG;
            Log.e(str, "Invalid caller type: " + callerType);
            return false;
        }
        this.mNetdEventCallbackList[callerType] = callback;
        return true;
    }

    public synchronized boolean removeNetdEventCallback(int callerType) {
        if (!isValidCallerType(callerType)) {
            String str = TAG;
            Log.e(str, "Invalid caller type: " + callerType);
            return false;
        }
        this.mNetdEventCallbackList[callerType] = null;
        return true;
    }

    private static boolean isValidCallerType(int callerType) {
        for (int i : ALLOWED_CALLBACK_TYPES) {
            if (callerType == i) {
                return true;
            }
        }
        return false;
    }

    public NetdEventListenerService(Context context) {
        this((ConnectivityManager) context.getSystemService(ConnectivityManager.class));
        this.mContext = context;
    }

    @VisibleForTesting
    public NetdEventListenerService(ConnectivityManager cm) {
        this.mNetworkMetrics = new SparseArray<>();
        this.mNetworkMetricsSnapshots = new RingBuffer<>(NetworkMetricsSnapshot.class, 48);
        this.mLastSnapshot = 0;
        this.mWakeupStats = new ArrayMap<>();
        this.mWakeupEvents = new RingBuffer<>(WakeupEvent.class, 1024);
        this.mConnectTb = new TokenBucket(15000, CONNECT_LATENCY_BURST_LIMIT);
        this.mNetdEventCallbackList = new INetdEventCallback[ALLOWED_CALLBACK_TYPES.length];
        this.mCm = cm;
    }

    private static long projectSnapshotTime(long timeMs) {
        return (timeMs / 300000) * 300000;
    }

    private NetworkMetrics getMetricsForNetwork(long timeMs, int netId) {
        collectPendingMetricsSnapshot(timeMs);
        NetworkMetrics metrics = this.mNetworkMetrics.get(netId);
        if (metrics != null) {
            return metrics;
        }
        NetworkMetrics metrics2 = new NetworkMetrics(netId, getTransports(netId), this.mConnectTb);
        this.mNetworkMetrics.put(netId, metrics2);
        return metrics2;
    }

    private NetworkMetricsSnapshot[] getNetworkMetricsSnapshots() {
        collectPendingMetricsSnapshot(System.currentTimeMillis());
        return (NetworkMetricsSnapshot[]) this.mNetworkMetricsSnapshots.toArray();
    }

    private void collectPendingMetricsSnapshot(long timeMs) {
        if (Math.abs(timeMs - this.mLastSnapshot) > 300000) {
            this.mLastSnapshot = projectSnapshotTime(timeMs);
            NetworkMetricsSnapshot snapshot = NetworkMetricsSnapshot.collect(this.mLastSnapshot, this.mNetworkMetrics);
            if (!snapshot.stats.isEmpty()) {
                this.mNetworkMetricsSnapshots.append(snapshot);
            }
        }
    }

    public synchronized void onDnsEvent(int netId, int eventType, int returnCode, int latencyMs, String hostname, String[] ipAddresses, int ipAddressesCount, int uid) throws RemoteException {
        int i;
        int i2 = netId;
        int i3 = returnCode;
        int i4 = latencyMs;
        synchronized (this) {
            long timestamp = System.currentTimeMillis();
            getMetricsForNetwork(timestamp, i2).addDnsResult(eventType, i3, i4);
            INetdEventCallback[] iNetdEventCallbackArr = this.mNetdEventCallbackList;
            int length = iNetdEventCallbackArr.length;
            int i5 = 0;
            while (i5 < length) {
                INetdEventCallback callback = iNetdEventCallbackArr[i5];
                if (callback != null) {
                    i = i5;
                    callback.onDnsEvent(hostname, ipAddresses, ipAddressesCount, timestamp, uid);
                } else {
                    i = i5;
                }
                i5 = i + 1;
            }
            if (this.mContext != null) {
                HwServiceFactory.getHwConnectivityManager().onDnsEvent(this.mContext, i3, i4, i2);
            }
        }
    }

    public synchronized void onPrivateDnsValidationEvent(int netId, String ipAddress, String hostname, boolean validated) throws RemoteException {
        for (INetdEventCallback callback : this.mNetdEventCallbackList) {
            if (callback != null) {
                callback.onPrivateDnsValidationEvent(netId, ipAddress, hostname, validated);
            }
        }
    }

    public synchronized void onConnectEvent(int netId, int error, int latencyMs, String ipAddr, int port, int uid) throws RemoteException {
        synchronized (this) {
            long timestamp = System.currentTimeMillis();
            String str = ipAddr;
            getMetricsForNetwork(timestamp, netId).addConnectResult(error, latencyMs, str);
            for (INetdEventCallback callback : this.mNetdEventCallbackList) {
                if (callback != null) {
                    callback.onConnectEvent(str, port, timestamp, uid);
                }
            }
        }
    }

    public synchronized void onWakeupEvent(String prefix, int uid, int ethertype, int ipNextHeader, byte[] dstHw, String srcIp, String dstIp, int srcPort, int dstPort, long timestampNs) {
        long timestampMs;
        synchronized (this) {
            String iface = prefix.replaceFirst(WAKEUP_EVENT_IFACE_PREFIX, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            if (timestampNs > 0) {
                timestampMs = timestampNs / 1000000;
            } else {
                timestampMs = System.currentTimeMillis();
            }
            long timestampMs2 = timestampMs;
            WakeupEvent event = new WakeupEvent();
            event.iface = iface;
            event.timestampMs = timestampMs2;
            int i = uid;
            event.uid = i;
            int i2 = ethertype;
            event.ethertype = i2;
            event.dstHwAddr = MacAddress.fromBytes(dstHw);
            event.srcIp = srcIp;
            event.dstIp = dstIp;
            event.ipNextHeader = ipNextHeader;
            event.srcPort = srcPort;
            event.dstPort = dstPort;
            addWakeupEvent(event);
            long j = timestampMs2;
            StatsLog.write(44, i, iface, i2, event.dstHwAddr.toString(), srcIp, dstIp, ipNextHeader, srcPort, dstPort);
        }
    }

    public synchronized void onTcpSocketStatsEvent(int[] networkIds, int[] sentPackets, int[] lostPackets, int[] rttsUs, int[] sentAckDiffsMs) {
        if (networkIds.length == sentPackets.length && networkIds.length == lostPackets.length && networkIds.length == rttsUs.length) {
            if (networkIds.length == sentAckDiffsMs.length) {
                long timestamp = System.currentTimeMillis();
                for (int i = 0; i < networkIds.length; i++) {
                    int netId = networkIds[i];
                    getMetricsForNetwork(timestamp, netId).addTcpStatsResult(sentPackets[i], lostPackets[i], rttsUs[i], sentAckDiffsMs[i]);
                }
                return;
            }
        }
        Log.e(TAG, "Mismatched lengths of TCP socket stats data arrays");
    }

    private void addWakeupEvent(WakeupEvent event) {
        String iface = event.iface;
        this.mWakeupEvents.append(event);
        WakeupStats stats = this.mWakeupStats.get(iface);
        if (stats == null) {
            stats = new WakeupStats(iface);
            this.mWakeupStats.put(iface, stats);
        }
        stats.countEvent(event);
    }

    public synchronized void flushStatistics(List<IpConnectivityLogClass.IpConnectivityEvent> events) {
        for (int i = 0; i < this.mNetworkMetrics.size(); i++) {
            ConnectStats stats = this.mNetworkMetrics.valueAt(i).connectMetrics;
            if (stats.eventCount != 0) {
                events.add(IpConnectivityEventBuilder.toProto(stats));
            }
        }
        for (int i2 = 0; i2 < this.mNetworkMetrics.size(); i2++) {
            DnsEvent ev = this.mNetworkMetrics.valueAt(i2).dnsMetrics;
            if (ev.eventCount != 0) {
                events.add(IpConnectivityEventBuilder.toProto(ev));
            }
        }
        for (int i3 = 0; i3 < this.mWakeupStats.size(); i3++) {
            events.add(IpConnectivityEventBuilder.toProto(this.mWakeupStats.valueAt(i3)));
        }
        this.mNetworkMetrics.clear();
        this.mWakeupStats.clear();
    }

    public synchronized void list(PrintWriter pw) {
        pw.println("dns/connect events:");
        for (int i = 0; i < this.mNetworkMetrics.size(); i++) {
            pw.println(this.mNetworkMetrics.valueAt(i).connectMetrics);
        }
        for (int i2 = 0; i2 < this.mNetworkMetrics.size(); i2++) {
            pw.println(this.mNetworkMetrics.valueAt(i2).dnsMetrics);
        }
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("network statistics:");
        for (NetworkMetricsSnapshot s : getNetworkMetricsSnapshots()) {
            pw.println(s);
        }
        pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        pw.println("packet wakeup events:");
        for (int i3 = 0; i3 < this.mWakeupStats.size(); i3++) {
            pw.println(this.mWakeupStats.valueAt(i3));
        }
        for (WakeupEvent wakeup : (WakeupEvent[]) this.mWakeupEvents.toArray()) {
            pw.println(wakeup);
        }
    }

    public synchronized void listAsProtos(PrintWriter pw) {
        for (int i = 0; i < this.mNetworkMetrics.size(); i++) {
            pw.print(IpConnectivityEventBuilder.toProto(this.mNetworkMetrics.valueAt(i).connectMetrics));
        }
        for (int i2 = 0; i2 < this.mNetworkMetrics.size(); i2++) {
            pw.print(IpConnectivityEventBuilder.toProto(this.mNetworkMetrics.valueAt(i2).dnsMetrics));
        }
        for (int i3 = 0; i3 < this.mWakeupStats.size(); i3++) {
            pw.print(IpConnectivityEventBuilder.toProto(this.mWakeupStats.valueAt(i3)));
        }
    }

    private long getTransports(int netId) {
        NetworkCapabilities nc = this.mCm.getNetworkCapabilities(new Network(netId));
        if (nc == null) {
            return 0;
        }
        return BitUtils.packBits(nc.getTransportTypes());
    }

    private static void maybeLog(String s, Object... args) {
    }
}
