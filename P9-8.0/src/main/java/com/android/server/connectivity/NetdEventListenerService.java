package com.android.server.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetdEventCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.metrics.ConnectStats;
import android.net.metrics.DnsEvent;
import android.net.metrics.INetdEventListener.Stub;
import android.os.RemoteException;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.BitUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.TokenBucket;
import com.android.server.HwServiceFactory;
import com.android.server.connectivity.metrics.nano.IpConnectivityLogClass.IpConnectivityEvent;
import java.io.PrintWriter;
import java.util.List;
import java.util.function.Function;

public class NetdEventListenerService extends Stub {
    private static final int CONNECT_LATENCY_BURST_LIMIT = 5000;
    private static final int CONNECT_LATENCY_FILL_RATE = 15000;
    private static final int CONNECT_LATENCY_MAXIMUM_RECORDS = 20000;
    private static final boolean DBG = false;
    private static final int INITIAL_DNS_BATCH_SIZE = 100;
    public static final String SERVICE_NAME = "netd_listener";
    private static final String TAG = NetdEventListenerService.class.getSimpleName();
    private static final boolean VDBG = false;
    private final ConnectivityManager mCm;
    @GuardedBy("this")
    private final SparseArray<ConnectStats> mConnectEvents;
    @GuardedBy("this")
    private final TokenBucket mConnectTb;
    private Context mContext;
    @GuardedBy("this")
    private final SparseArray<DnsEvent> mDnsEvents;
    @GuardedBy("this")
    private INetdEventCallback mNetdEventCallback;

    public synchronized boolean registerNetdEventCallback(INetdEventCallback callback) {
        this.mNetdEventCallback = callback;
        return true;
    }

    public synchronized boolean unregisterNetdEventCallback() {
        this.mNetdEventCallback = null;
        return true;
    }

    public NetdEventListenerService(Context context) {
        this((ConnectivityManager) context.getSystemService(ConnectivityManager.class));
        this.mContext = context;
    }

    public NetdEventListenerService(ConnectivityManager cm) {
        this.mDnsEvents = new SparseArray();
        this.mConnectEvents = new SparseArray();
        this.mConnectTb = new TokenBucket(15000, CONNECT_LATENCY_BURST_LIMIT);
        this.mCm = cm;
    }

    public synchronized void onDnsEvent(int netId, int eventType, int returnCode, int latencyMs, String hostname, String[] ipAddresses, int ipAddressesCount, int uid) throws RemoteException {
        maybeVerboseLog("onDnsEvent(%d, %d, %d, %dms)", Integer.valueOf(netId), Integer.valueOf(eventType), Integer.valueOf(returnCode), Integer.valueOf(latencyMs));
        DnsEvent dnsEvent = (DnsEvent) this.mDnsEvents.get(netId);
        if (dnsEvent == null) {
            dnsEvent = makeDnsEvent(netId);
            this.mDnsEvents.put(netId, dnsEvent);
        }
        dnsEvent.addResult((byte) eventType, (byte) returnCode, latencyMs);
        if (this.mNetdEventCallback != null) {
            this.mNetdEventCallback.onDnsEvent(hostname, ipAddresses, ipAddressesCount, System.currentTimeMillis(), uid);
        }
        if (this.mContext != null) {
            HwServiceFactory.getHwConnectivityManager().onDnsEvent(this.mContext, returnCode, latencyMs);
        }
    }

    public synchronized void onConnectEvent(int netId, int error, int latencyMs, String ipAddr, int port, int uid) throws RemoteException {
        maybeVerboseLog("onConnectEvent(%d, %d, %dms)", Integer.valueOf(netId), Integer.valueOf(error), Integer.valueOf(latencyMs));
        ConnectStats connectStats = (ConnectStats) this.mConnectEvents.get(netId);
        if (connectStats == null) {
            connectStats = makeConnectStats(netId);
            this.mConnectEvents.put(netId, connectStats);
        }
        connectStats.addEvent(error, latencyMs, ipAddr);
        if (this.mNetdEventCallback != null) {
            this.mNetdEventCallback.onConnectEvent(ipAddr, port, System.currentTimeMillis(), uid);
        }
    }

    public synchronized void flushStatistics(List<IpConnectivityEvent> events) {
        flushProtos(events, this.mConnectEvents, new -$Lambda$VjDKAdE1DIPju6OxZuMswrYP1XY());
        flushProtos(events, this.mDnsEvents, new Function() {
            public final Object apply(Object obj) {
                return $m$0(obj);
            }
        });
    }

    public synchronized void dump(PrintWriter writer) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println(TAG + ":");
        pw.increaseIndent();
        list(pw);
        pw.decreaseIndent();
    }

    static /* synthetic */ Object lambda$-com_android_server_connectivity_NetdEventListenerService_6923(ConnectStats x) {
        return x;
    }

    public synchronized void list(PrintWriter pw) {
        listEvents(pw, this.mConnectEvents, new Function() {
            public final Object apply(Object obj) {
                return $m$0(obj);
            }
        });
        listEvents(pw, this.mDnsEvents, new Function() {
            public final Object apply(Object obj) {
                return $m$0(obj);
            }
        });
    }

    static /* synthetic */ Object lambda$-com_android_server_connectivity_NetdEventListenerService_6969(DnsEvent x) {
        return x;
    }

    public synchronized void listAsProtos(PrintWriter pw) {
        listEvents(pw, this.mConnectEvents, new Function() {
            public final Object apply(Object obj) {
                return $m$0(obj);
            }
        });
        listEvents(pw, this.mDnsEvents, new Function() {
            public final Object apply(Object obj) {
                return $m$0(obj);
            }
        });
    }

    private static <T> void flushProtos(List<IpConnectivityEvent> out, SparseArray<T> in, Function<T, IpConnectivityEvent> mapper) {
        for (int i = 0; i < in.size(); i++) {
            out.add((IpConnectivityEvent) mapper.apply(in.valueAt(i)));
        }
        in.clear();
    }

    public static <T> void listEvents(PrintWriter pw, SparseArray<T> events, Function<T, Object> mapper) {
        for (int i = 0; i < events.size(); i++) {
            pw.println(mapper.apply(events.valueAt(i)).toString());
        }
    }

    private ConnectStats makeConnectStats(int netId) {
        return new ConnectStats(netId, getTransports(netId), this.mConnectTb, CONNECT_LATENCY_MAXIMUM_RECORDS);
    }

    private DnsEvent makeDnsEvent(int netId) {
        return new DnsEvent(netId, getTransports(netId), 100);
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

    private static void maybeVerboseLog(String s, Object... args) {
    }
}
