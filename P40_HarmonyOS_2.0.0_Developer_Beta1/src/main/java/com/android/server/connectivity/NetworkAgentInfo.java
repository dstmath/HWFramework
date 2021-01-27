package com.android.server.connectivity;

import android.content.Context;
import android.net.IDnsResolver;
import android.net.INetd;
import android.net.INetworkMonitor;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkMonitorManager;
import android.net.NetworkRequest;
import android.net.NetworkState;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Messenger;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.WakeupMessage;
import com.android.server.BatteryService;
import com.android.server.ConnectivityService;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public class NetworkAgentInfo implements Comparable<NetworkAgentInfo> {
    private static final boolean ADD = true;
    public static final int EVENT_NETWORK_LINGER_COMPLETE = 1001;
    private static final boolean REMOVE = false;
    private static final String TAG = ConnectivityService.class.getSimpleName();
    private static final boolean VDBG = false;
    public static final int WIFI_AP_AI_DEVICE_SCORE = 35;
    private static boolean isEthernetPriority = "ethernet".equals(SystemProperties.get("persist.network.firstpriority", "ethernet"));
    private static boolean isTv = "tv".equals(SystemProperties.get("ro.build.characteristics", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR));
    public final AsyncChannel asyncChannel;
    public boolean avoidUnvalidated;
    public volatile boolean captivePortalValidationPending;
    public final Nat464Xlat clatd;
    public boolean created;
    private int currentScore;
    public boolean everCaptivePortalDetected;
    public boolean everConnected;
    public boolean everValidated;
    public final int factorySerialNumber;
    public boolean lastCaptivePortalDetected;
    public boolean lastValidated;
    public LinkProperties linkProperties;
    private final ConnectivityService mConnService;
    private final Context mContext;
    private final Handler mHandler;
    private long mLingerExpiryMs;
    private WakeupMessage mLingerMessage;
    private final SparseArray<LingerTimer> mLingerTimerForRequest = new SparseArray<>();
    private final SortedSet<LingerTimer> mLingerTimers = new TreeSet();
    private boolean mLingering;
    private volatile NetworkMonitorManager mNetworkMonitor;
    private final SparseArray<NetworkRequest> mNetworkRequests = new SparseArray<>();
    private int mNumBackgroundNetworkRequests = 0;
    private int mNumRequestNetworkRequests = 0;
    public final Messenger messenger;
    public final Network network;
    public NetworkCapabilities networkCapabilities;
    public NetworkInfo networkInfo;
    public final NetworkMisc networkMisc;
    public boolean partialConnectivity;

    public static class LingerTimer implements Comparable<LingerTimer> {
        public final long expiryMs;
        public final NetworkRequest request;

        public LingerTimer(NetworkRequest request2, long expiryMs2) {
            this.request = request2;
            this.expiryMs = expiryMs2;
        }

        @Override // java.lang.Object
        public boolean equals(Object o) {
            if (!(o instanceof LingerTimer)) {
                return false;
            }
            LingerTimer other = (LingerTimer) o;
            if (this.request.requestId == other.request.requestId && this.expiryMs == other.expiryMs) {
                return true;
            }
            return false;
        }

        @Override // java.lang.Object
        public int hashCode() {
            return Objects.hash(Integer.valueOf(this.request.requestId), Long.valueOf(this.expiryMs));
        }

        public int compareTo(LingerTimer other) {
            long j = this.expiryMs;
            long j2 = other.expiryMs;
            if (j != j2) {
                return Long.compare(j, j2);
            }
            return Integer.compare(this.request.requestId, other.request.requestId);
        }

        @Override // java.lang.Object
        public String toString() {
            return String.format("%s, expires %dms", this.request.toString(), Long.valueOf(this.expiryMs - SystemClock.elapsedRealtime()));
        }
    }

    public NetworkAgentInfo(Messenger messenger2, AsyncChannel ac, Network net, NetworkInfo info, LinkProperties lp, NetworkCapabilities nc, int score, Context context, Handler handler, NetworkMisc misc, ConnectivityService connService, INetd netd, IDnsResolver dnsResolver, INetworkManagementService nms, int factorySerialNumber2) {
        this.messenger = messenger2;
        this.asyncChannel = ac;
        this.network = net;
        this.networkInfo = info;
        this.linkProperties = lp;
        this.networkCapabilities = nc;
        this.currentScore = score;
        this.clatd = new Nat464Xlat(this, netd, dnsResolver, nms);
        this.mConnService = connService;
        this.mContext = context;
        this.mHandler = handler;
        this.networkMisc = misc;
        this.factorySerialNumber = factorySerialNumber2;
    }

    public void onNetworkMonitorCreated(INetworkMonitor networkMonitor) {
        this.mNetworkMonitor = new NetworkMonitorManager(networkMonitor);
    }

    public void setNetworkCapabilities(NetworkCapabilities nc) {
        this.networkCapabilities = nc;
        NetworkMonitorManager nm = this.mNetworkMonitor;
        if (nm != null) {
            nm.notifyNetworkCapabilitiesChanged(nc);
        }
    }

    public ConnectivityService connService() {
        return this.mConnService;
    }

    public NetworkMisc netMisc() {
        return this.networkMisc;
    }

    public Handler handler() {
        return this.mHandler;
    }

    public Network network() {
        return this.network;
    }

    public NetworkMonitorManager networkMonitor() {
        return this.mNetworkMonitor;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.connectivity.NetworkAgentInfo$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkRequest$Type = new int[NetworkRequest.Type.values().length];

        static {
            try {
                $SwitchMap$android$net$NetworkRequest$Type[NetworkRequest.Type.REQUEST.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$NetworkRequest$Type[NetworkRequest.Type.BACKGROUND_REQUEST.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$net$NetworkRequest$Type[NetworkRequest.Type.TRACK_DEFAULT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$net$NetworkRequest$Type[NetworkRequest.Type.LISTEN.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$net$NetworkRequest$Type[NetworkRequest.Type.NONE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    private void updateRequestCounts(boolean add, NetworkRequest request) {
        int delta = add ? 1 : -1;
        int i = AnonymousClass1.$SwitchMap$android$net$NetworkRequest$Type[request.type.ordinal()];
        if (i == 1) {
            this.mNumRequestNetworkRequests += delta;
        } else if (i == 2) {
            this.mNumRequestNetworkRequests += delta;
            this.mNumBackgroundNetworkRequests += delta;
        } else if (i != 3 && i != 4) {
            Log.wtf(TAG, "Unhandled request type " + request.type);
        }
    }

    public boolean addRequest(NetworkRequest networkRequest) {
        NetworkRequest existing = this.mNetworkRequests.get(networkRequest.requestId);
        if (existing == networkRequest) {
            return false;
        }
        if (existing != null) {
            Log.wtf(TAG, String.format("Duplicate requestId for %s and %s on %s", networkRequest, existing, name()));
            updateRequestCounts(false, existing);
        }
        this.mNetworkRequests.put(networkRequest.requestId, networkRequest);
        updateRequestCounts(true, networkRequest);
        return true;
    }

    public void removeRequest(int requestId) {
        NetworkRequest existing = this.mNetworkRequests.get(requestId);
        if (existing != null) {
            updateRequestCounts(false, existing);
            this.mNetworkRequests.remove(requestId);
            if (existing.isRequest()) {
                unlingerRequest(existing);
            }
        }
    }

    public boolean isSatisfyingRequest(int id) {
        return this.mNetworkRequests.get(id) != null;
    }

    public NetworkRequest requestAt(int index) {
        return this.mNetworkRequests.valueAt(index);
    }

    public int numRequestNetworkRequests() {
        return this.mNumRequestNetworkRequests;
    }

    public int numBackgroundNetworkRequests() {
        return this.mNumBackgroundNetworkRequests;
    }

    public int numForegroundNetworkRequests() {
        return this.mNumRequestNetworkRequests - this.mNumBackgroundNetworkRequests;
    }

    public int numNetworkRequests() {
        return this.mNetworkRequests.size();
    }

    public boolean isBackgroundNetwork() {
        return !isVPN() && numForegroundNetworkRequests() == 0 && this.mNumBackgroundNetworkRequests > 0 && !isLingering();
    }

    public boolean isSuspended() {
        return this.networkInfo.getState() == NetworkInfo.State.SUSPENDED;
    }

    public boolean satisfies(NetworkRequest request) {
        return this.created && request.networkCapabilities.satisfiedByNetworkCapabilities(this.networkCapabilities);
    }

    public boolean satisfiesImmutableCapabilitiesOf(NetworkRequest request) {
        return this.created && request.networkCapabilities.satisfiedByImmutableNetworkCapabilities(this.networkCapabilities);
    }

    public boolean isVPN() {
        return this.networkCapabilities.hasTransport(4);
    }

    public boolean isEthernet() {
        return this.networkCapabilities.hasTransport(3);
    }

    private int getCurrentScore(boolean pretendValidated) {
        if (isTv && isEthernet() && isEthernetPriority) {
            return 70;
        }
        NetworkMisc networkMisc2 = this.networkMisc;
        if (networkMisc2 != null && networkMisc2.acceptUnvalidated) {
            return 100;
        }
        NetworkMisc networkMisc3 = this.networkMisc;
        if (networkMisc3 != null && networkMisc3.explicitlySelected && pretendValidated) {
            return 100;
        }
        int score = this.currentScore;
        if (!this.lastValidated && !pretendValidated && !ignoreWifiUnvalidationPenalty() && !isVPN()) {
            score -= 40;
        }
        if (score < 0) {
            return 0;
        }
        return score;
    }

    private boolean ignoreWifiUnvalidationPenalty() {
        return (this.networkCapabilities.hasTransport(1) && this.networkCapabilities.hasCapability(12)) && !(this.mConnService.avoidBadWifi() || this.avoidUnvalidated) && this.everValidated;
    }

    public int getCurrentScore() {
        return getCurrentScore(false);
    }

    public int getCurrentScoreAsValidated() {
        return getCurrentScore(true);
    }

    public void setCurrentScore(int newScore) {
        this.currentScore = newScore;
    }

    public boolean isInternet() {
        return this.networkCapabilities.hasCapability(12);
    }

    public NetworkState getNetworkState() {
        NetworkState networkState;
        synchronized (this) {
            networkState = new NetworkState(new NetworkInfo(this.networkInfo), new LinkProperties(this.linkProperties), new NetworkCapabilities(this.networkCapabilities), this.network, this.networkMisc != null ? this.networkMisc.subscriberId : null, (String) null);
        }
        return networkState;
    }

    public void lingerRequest(NetworkRequest request, long now, long duration) {
        if (this.mLingerTimerForRequest.get(request.requestId) != null) {
            String str = TAG;
            Log.wtf(str, name() + ": request " + request.requestId + " already lingered");
        }
        LingerTimer timer = new LingerTimer(request, now + duration);
        this.mLingerTimers.add(timer);
        this.mLingerTimerForRequest.put(request.requestId, timer);
    }

    public boolean unlingerRequest(NetworkRequest request) {
        LingerTimer timer = this.mLingerTimerForRequest.get(request.requestId);
        if (timer == null) {
            return false;
        }
        this.mLingerTimers.remove(timer);
        this.mLingerTimerForRequest.remove(request.requestId);
        return true;
    }

    public long getLingerExpiry() {
        return this.mLingerExpiryMs;
    }

    public void updateLingerTimer() {
        long newExpiry = this.mLingerTimers.isEmpty() ? 0 : this.mLingerTimers.last().expiryMs;
        if (newExpiry != this.mLingerExpiryMs) {
            WakeupMessage wakeupMessage = this.mLingerMessage;
            if (wakeupMessage != null) {
                wakeupMessage.cancel();
                this.mLingerMessage = null;
            }
            if (newExpiry > 0) {
                ConnectivityService connectivityService = this.mConnService;
                Context context = this.mContext;
                Handler handler = this.mHandler;
                this.mLingerMessage = connectivityService.makeWakeupMessage(context, handler, "NETWORK_LINGER_COMPLETE." + this.network.netId, EVENT_NETWORK_LINGER_COMPLETE, this);
                this.mLingerMessage.schedule(newExpiry);
            }
            this.mLingerExpiryMs = newExpiry;
        }
    }

    public void linger() {
        this.mLingering = true;
    }

    public void unlinger() {
        this.mLingering = false;
    }

    public boolean isLingering() {
        return this.mLingering;
    }

    public void clearLingerState() {
        WakeupMessage wakeupMessage = this.mLingerMessage;
        if (wakeupMessage != null) {
            wakeupMessage.cancel();
            this.mLingerMessage = null;
        }
        this.mLingerTimers.clear();
        this.mLingerTimerForRequest.clear();
        updateLingerTimer();
        this.mLingering = false;
    }

    public void dumpLingerTimers(PrintWriter pw) {
        for (LingerTimer timer : this.mLingerTimers) {
            pw.println(timer);
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return "NetworkAgentInfo{ ni{" + this.networkInfo + "}  network{" + this.network + "}  nethandle{" + this.network.getNetworkHandle() + "}  nc{" + this.networkCapabilities + "}  Score{" + getCurrentScore() + "}  everValidated{" + this.everValidated + "}  lastValidated{" + this.lastValidated + "}  created{" + this.created + "} lingering{" + isLingering() + "} explicitlySelected{" + this.networkMisc.explicitlySelected + "} acceptUnvalidated{" + this.networkMisc.acceptUnvalidated + "} everCaptivePortalDetected{" + this.everCaptivePortalDetected + "} lastCaptivePortalDetected{" + this.lastCaptivePortalDetected + "} captivePortalValidationPending{" + this.captivePortalValidationPending + "} partialConnectivity{" + this.partialConnectivity + "} acceptPartialConnectivity{" + this.networkMisc.acceptPartialConnectivity + "} clat{" + this.clatd + "} }";
    }

    public String name() {
        return "NetworkAgentInfo [" + this.networkInfo.getTypeName() + " (" + this.networkInfo.getSubtypeName() + ") - " + Objects.toString(this.network) + "]";
    }

    public int compareTo(NetworkAgentInfo other) {
        return other.getCurrentScore() - getCurrentScore();
    }
}
