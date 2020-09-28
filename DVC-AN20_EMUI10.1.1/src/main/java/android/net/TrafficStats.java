package android.net;

import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.net.INetworkStatsService;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.server.NetworkManagementSocketTagger;
import dalvik.system.SocketTagger;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

public class TrafficStats {
    @Deprecated
    public static final long GB_IN_BYTES = 1073741824;
    @Deprecated
    public static final long KB_IN_BYTES = 1024;
    private static final String LOOPBACK_IFACE = "lo";
    @Deprecated
    public static final long MB_IN_BYTES = 1048576;
    @Deprecated
    public static final long PB_IN_BYTES = 1125899906842624L;
    @SystemApi
    public static final int TAG_NETWORK_STACK_IMPERSONATION_RANGE_END = -113;
    @SystemApi
    public static final int TAG_NETWORK_STACK_IMPERSONATION_RANGE_START = -128;
    @SystemApi
    public static final int TAG_NETWORK_STACK_RANGE_END = -257;
    @SystemApi
    public static final int TAG_NETWORK_STACK_RANGE_START = -768;
    public static final int TAG_SYSTEM_APP = -251;
    public static final int TAG_SYSTEM_BACKUP = -253;
    public static final int TAG_SYSTEM_DOWNLOAD = -255;
    @SystemApi
    public static final int TAG_SYSTEM_IMPERSONATION_RANGE_END = -241;
    @SystemApi
    public static final int TAG_SYSTEM_IMPERSONATION_RANGE_START = -256;
    public static final int TAG_SYSTEM_MEDIA = -254;
    public static final int TAG_SYSTEM_PROBE = -190;
    public static final int TAG_SYSTEM_RESTORE = -252;
    @Deprecated
    public static final long TB_IN_BYTES = 1099511627776L;
    private static final int TYPE_RX_BYTES = 0;
    private static final int TYPE_RX_PACKETS = 1;
    private static final int TYPE_TCP_RX_PACKETS = 4;
    private static final int TYPE_TCP_TX_PACKETS = 5;
    private static final int TYPE_TX_BYTES = 2;
    private static final int TYPE_TX_PACKETS = 3;
    public static final int UID_REMOVED = -4;
    public static final int UID_TETHERING = -5;
    public static final int UNSUPPORTED = -1;
    private static NetworkStats sActiveProfilingStart;
    private static Object sProfilingLock = new Object();
    private static INetworkStatsService sStatsService;

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 130143562)
    private static synchronized INetworkStatsService getStatsService() {
        INetworkStatsService iNetworkStatsService;
        synchronized (TrafficStats.class) {
            if (sStatsService == null) {
                sStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
            }
            iNetworkStatsService = sStatsService;
        }
        return iNetworkStatsService;
    }

    public static void setThreadStatsTag(int tag) {
        NetworkManagementSocketTagger.setThreadSocketStatsTag(tag);
    }

    public static int getAndSetThreadStatsTag(int tag) {
        return NetworkManagementSocketTagger.setThreadSocketStatsTag(tag);
    }

    @SystemApi
    public static void setThreadStatsTagBackup() {
        setThreadStatsTag(TAG_SYSTEM_BACKUP);
    }

    @SystemApi
    public static void setThreadStatsTagRestore() {
        setThreadStatsTag(TAG_SYSTEM_RESTORE);
    }

    @SystemApi
    public static void setThreadStatsTagApp() {
        setThreadStatsTag(TAG_SYSTEM_APP);
    }

    public static int getThreadStatsTag() {
        return NetworkManagementSocketTagger.getThreadSocketStatsTag();
    }

    public static void clearThreadStatsTag() {
        NetworkManagementSocketTagger.setThreadSocketStatsTag(-1);
    }

    @SuppressLint({"Doclava125"})
    public static void setThreadStatsUid(int uid) {
        NetworkManagementSocketTagger.setThreadSocketStatsUid(uid);
    }

    public static int getThreadStatsUid() {
        return NetworkManagementSocketTagger.getThreadSocketStatsUid();
    }

    @Deprecated
    public static void setThreadStatsUidSelf() {
        setThreadStatsUid(Process.myUid());
    }

    @SuppressLint({"Doclava125"})
    public static void clearThreadStatsUid() {
        NetworkManagementSocketTagger.setThreadSocketStatsUid(-1);
    }

    public static void tagSocket(Socket socket) throws SocketException {
        SocketTagger.get().tag(socket);
    }

    public static void untagSocket(Socket socket) throws SocketException {
        SocketTagger.get().untag(socket);
    }

    public static void tagDatagramSocket(DatagramSocket socket) throws SocketException {
        SocketTagger.get().tag(socket);
    }

    public static void untagDatagramSocket(DatagramSocket socket) throws SocketException {
        SocketTagger.get().untag(socket);
    }

    public static void tagFileDescriptor(FileDescriptor fd) throws IOException {
        SocketTagger.get().tag(fd);
    }

    public static void untagFileDescriptor(FileDescriptor fd) throws IOException {
        SocketTagger.get().untag(fd);
    }

    public static void startDataProfiling(Context context) {
        synchronized (sProfilingLock) {
            if (sActiveProfilingStart == null) {
                sActiveProfilingStart = getDataLayerSnapshotForUid(context);
            } else {
                throw new IllegalStateException("already profiling data");
            }
        }
    }

    public static NetworkStats stopDataProfiling(Context context) {
        NetworkStats profilingDelta;
        synchronized (sProfilingLock) {
            if (sActiveProfilingStart != null) {
                profilingDelta = NetworkStats.subtract(getDataLayerSnapshotForUid(context), sActiveProfilingStart, null, null);
                sActiveProfilingStart = null;
            } else {
                throw new IllegalStateException("not profiling data");
            }
        }
        return profilingDelta;
    }

    public static void incrementOperationCount(int operationCount) {
        incrementOperationCount(getThreadStatsTag(), operationCount);
    }

    public static void incrementOperationCount(int tag, int operationCount) {
        try {
            getStatsService().incrementOperationCount(Process.myUid(), tag, operationCount);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void closeQuietly(INetworkStatsSession session) {
        if (session != null) {
            try {
                session.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e) {
            }
        }
    }

    private static long addIfSupported(long stat) {
        if (stat == -1) {
            return 0;
        }
        return stat;
    }

    public static long getMobileTxPackets() {
        long total = 0;
        for (String iface : getMobileIfaces()) {
            total += addIfSupported(getTxPackets(iface));
        }
        return total;
    }

    public static long getMobileRxPackets() {
        long total = 0;
        for (String iface : getMobileIfaces()) {
            total += addIfSupported(getRxPackets(iface));
        }
        return total;
    }

    public static long getMobileTxBytes() {
        long total = 0;
        for (String iface : getMobileIfaces()) {
            total += addIfSupported(getTxBytes(iface));
        }
        return total;
    }

    public static long getMobileRxBytes() {
        long total = 0;
        for (String iface : getMobileIfaces()) {
            total += addIfSupported(getRxBytes(iface));
        }
        return total;
    }

    @UnsupportedAppUsage
    public static long getMobileTcpRxPackets() {
        long total = 0;
        for (String iface : getMobileIfaces()) {
            try {
                total += addIfSupported(getStatsService().getIfaceStats(iface, 4));
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return total;
    }

    @UnsupportedAppUsage
    public static long getMobileTcpTxPackets() {
        long total = 0;
        for (String iface : getMobileIfaces()) {
            try {
                total += addIfSupported(getStatsService().getIfaceStats(iface, 5));
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return total;
    }

    public static long getTxPackets(String iface) {
        try {
            return getStatsService().getIfaceStats(iface, 3);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getRxPackets(String iface) {
        try {
            return getStatsService().getIfaceStats(iface, 1);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public static long getTxBytes(String iface) {
        try {
            return getStatsService().getIfaceStats(iface, 2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public static long getRxBytes(String iface) {
        try {
            return getStatsService().getIfaceStats(iface, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getLoopbackTxPackets() {
        try {
            return getStatsService().getIfaceStats(LOOPBACK_IFACE, 3);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getLoopbackRxPackets() {
        try {
            return getStatsService().getIfaceStats(LOOPBACK_IFACE, 1);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getLoopbackTxBytes() {
        try {
            return getStatsService().getIfaceStats(LOOPBACK_IFACE, 2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getLoopbackRxBytes() {
        try {
            return getStatsService().getIfaceStats(LOOPBACK_IFACE, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getTotalTxPackets() {
        try {
            return getStatsService().getTotalStats(3);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getTotalRxPackets() {
        try {
            return getStatsService().getTotalStats(1);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getTotalTxBytes() {
        try {
            return getStatsService().getTotalStats(2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getTotalRxBytes() {
        try {
            return getStatsService().getTotalStats(0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getUidTxBytes(int uid) {
        int callingUid = Process.myUid();
        if (callingUid != 1000 && callingUid != uid) {
            return -1;
        }
        try {
            return getStatsService().getUidStats(uid, 2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getUidRxBytes(int uid) {
        int callingUid = Process.myUid();
        if (callingUid != 1000 && callingUid != uid) {
            return -1;
        }
        try {
            return getStatsService().getUidStats(uid, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getUidTxPackets(int uid) {
        int callingUid = Process.myUid();
        if (callingUid != 1000 && callingUid != uid) {
            return -1;
        }
        try {
            return getStatsService().getUidStats(uid, 3);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long getUidRxPackets(int uid) {
        int callingUid = Process.myUid();
        if (callingUid != 1000 && callingUid != uid) {
            return -1;
        }
        try {
            return getStatsService().getUidStats(uid, 1);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public static long getUidTcpTxBytes(int uid) {
        return -1;
    }

    @Deprecated
    public static long getUidTcpRxBytes(int uid) {
        return -1;
    }

    @Deprecated
    public static long getUidUdpTxBytes(int uid) {
        return -1;
    }

    @Deprecated
    public static long getUidUdpRxBytes(int uid) {
        return -1;
    }

    @Deprecated
    public static long getUidTcpTxSegments(int uid) {
        return -1;
    }

    @Deprecated
    public static long getUidTcpRxSegments(int uid) {
        return -1;
    }

    @Deprecated
    public static long getUidUdpTxPackets(int uid) {
        return -1;
    }

    @Deprecated
    public static long getUidUdpRxPackets(int uid) {
        return -1;
    }

    private static NetworkStats getDataLayerSnapshotForUid(Context context) {
        try {
            return getStatsService().getDataLayerSnapshotForUid(Process.myUid());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 130143562)
    private static String[] getMobileIfaces() {
        try {
            return getStatsService().getMobileIfaces();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static String[] getMobileIfacesEx() {
        return getMobileIfaces();
    }
}
