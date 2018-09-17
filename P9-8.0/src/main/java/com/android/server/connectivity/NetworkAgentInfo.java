package com.android.server.connectivity;

import android.content.Context;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Type;
import android.net.NetworkState;
import android.os.Handler;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.WakeupMessage;
import com.android.server.ConnectivityService;
import com.android.server.HwServiceFactory;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public class NetworkAgentInfo implements Comparable<NetworkAgentInfo> {
    private static final /* synthetic */ int[] -android-net-NetworkRequest$TypeSwitchesValues = null;
    private static final boolean ADD = true;
    public static final int EVENT_NETWORK_LINGER_COMPLETE = 1001;
    private static final int MAXIMUM_NETWORK_SCORE = 100;
    private static final boolean REMOVE = false;
    private static final String TAG = ConnectivityService.class.getSimpleName();
    private static final int UNVALIDATED_SCORE_PENALTY = 40;
    private static final boolean VDBG = false;
    public final AsyncChannel asyncChannel;
    public boolean avoidUnvalidated;
    public Nat464Xlat clatd;
    public boolean created;
    private int currentScore;
    public boolean everCaptivePortalDetected;
    public boolean everConnected;
    public boolean everValidated;
    public boolean lastCaptivePortalDetected;
    public boolean lastValidated;
    public LinkProperties linkProperties;
    private final ConnectivityService mConnService;
    private final Context mContext;
    private final Handler mHandler;
    private long mLingerExpiryMs;
    private WakeupMessage mLingerMessage;
    private final SparseArray<LingerTimer> mLingerTimerForRequest = new SparseArray();
    private final SortedSet<LingerTimer> mLingerTimers = new TreeSet();
    private boolean mLingering;
    private final SparseArray<NetworkRequest> mNetworkRequests = new SparseArray();
    private int mNumBackgroundNetworkRequests = 0;
    private int mNumRequestNetworkRequests = 0;
    public final Messenger messenger;
    public final Network network;
    public NetworkCapabilities networkCapabilities;
    public NetworkInfo networkInfo;
    public final NetworkMisc networkMisc;
    public final NetworkMonitor networkMonitor;

    public static class LingerTimer implements Comparable<LingerTimer> {
        public final long expiryMs;
        public final NetworkRequest request;

        public LingerTimer(NetworkRequest request, long expiryMs) {
            this.request = request;
            this.expiryMs = expiryMs;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof LingerTimer)) {
                return false;
            }
            LingerTimer other = (LingerTimer) o;
            if (this.request.requestId == other.request.requestId && this.expiryMs == other.expiryMs) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(this.request.requestId), Long.valueOf(this.expiryMs)});
        }

        public int compareTo(LingerTimer other) {
            if (this.expiryMs != other.expiryMs) {
                return Long.compare(this.expiryMs, other.expiryMs);
            }
            return Integer.compare(this.request.requestId, other.request.requestId);
        }

        public String toString() {
            return String.format("%s, expires %dms", new Object[]{this.request.toString(), Long.valueOf(this.expiryMs - SystemClock.elapsedRealtime())});
        }
    }

    private static /* synthetic */ int[] -getandroid-net-NetworkRequest$TypeSwitchesValues() {
        if (-android-net-NetworkRequest$TypeSwitchesValues != null) {
            return -android-net-NetworkRequest$TypeSwitchesValues;
        }
        int[] iArr = new int[Type.values().length];
        try {
            iArr[Type.BACKGROUND_REQUEST.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Type.LISTEN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Type.NONE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Type.REQUEST.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Type.TRACK_DEFAULT.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        -android-net-NetworkRequest$TypeSwitchesValues = iArr;
        return iArr;
    }

    public NetworkAgentInfo(Messenger messenger, AsyncChannel ac, Network net, NetworkInfo info, LinkProperties lp, NetworkCapabilities nc, int score, Context context, Handler handler, NetworkMisc misc, NetworkRequest defaultRequest, ConnectivityService connService) {
        this.messenger = messenger;
        this.asyncChannel = ac;
        this.network = net;
        this.networkInfo = info;
        this.linkProperties = lp;
        this.networkCapabilities = nc;
        this.currentScore = score;
        this.mConnService = connService;
        this.mContext = context;
        this.mHandler = handler;
        this.networkMonitor = HwServiceFactory.getHwConnectivityManager().createHwNetworkMonitor(context, handler, this, defaultRequest);
        this.networkMisc = misc;
    }

    private void updateRequestCounts(boolean add, NetworkRequest request) {
        int delta = add ? 1 : -1;
        switch (-getandroid-net-NetworkRequest$TypeSwitchesValues()[request.type.ordinal()]) {
            case 1:
                this.mNumRequestNetworkRequests += delta;
                this.mNumBackgroundNetworkRequests += delta;
                return;
            case 2:
                return;
            case 4:
            case 5:
                this.mNumRequestNetworkRequests += delta;
                return;
            default:
                Log.wtf(TAG, "Unhandled request type " + request.type);
                return;
        }
    }

    public boolean addRequest(NetworkRequest networkRequest) {
        NetworkRequest existing = (NetworkRequest) this.mNetworkRequests.get(networkRequest.requestId);
        if (existing == networkRequest) {
            return false;
        }
        if (existing != null) {
            Log.wtf(TAG, String.format("Duplicate requestId for %s and %s on %s", new Object[]{networkRequest, existing, name()}));
            updateRequestCounts(false, existing);
        }
        this.mNetworkRequests.put(networkRequest.requestId, networkRequest);
        updateRequestCounts(true, networkRequest);
        return true;
    }

    public void removeRequest(int requestId) {
        NetworkRequest existing = (NetworkRequest) this.mNetworkRequests.get(requestId);
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
        return (NetworkRequest) this.mNetworkRequests.valueAt(index);
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
        return !isVPN() && numForegroundNetworkRequests() == 0 && this.mNumBackgroundNetworkRequests > 0;
    }

    public boolean satisfies(NetworkRequest request) {
        if (this.created) {
            return request.networkCapabilities.satisfiedByNetworkCapabilities(this.networkCapabilities);
        }
        return false;
    }

    public boolean satisfiesImmutableCapabilitiesOf(NetworkRequest request) {
        if (this.created) {
            return request.networkCapabilities.satisfiedByImmutableNetworkCapabilities(this.networkCapabilities);
        }
        return false;
    }

    public boolean isVPN() {
        return this.networkCapabilities.hasTransport(4);
    }

    public boolean isEthernet() {
        return this.networkCapabilities.hasTransport(3);
    }

    private int getCurrentScore(boolean pretendValidated) {
        if (this.networkMisc.explicitlySelected && (this.networkMisc.acceptUnvalidated || pretendValidated)) {
            return 100;
        }
        int score = this.currentScore;
        if (!(this.lastValidated || (pretendValidated ^ 1) == 0 || (ignoreWifiUnvalidationPenalty() ^ 1) == 0)) {
            score -= 40;
        }
        if (score < 0) {
            score = 0;
        }
        if (this.networkMisc != null && this.networkMisc.explicitlySelected) {
            score = 100;
        }
        return score;
    }

    private boolean ignoreWifiUnvalidationPenalty() {
        boolean isWifi;
        if (this.networkCapabilities.hasTransport(1)) {
            isWifi = this.networkCapabilities.hasCapability(12);
        } else {
            isWifi = false;
        }
        return (!isWifi || ((!this.mConnService.avoidBadWifi() ? this.avoidUnvalidated : 1) ^ 1) == 0) ? false : this.everValidated;
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
            networkState = new NetworkState(new NetworkInfo(this.networkInfo), new LinkProperties(this.linkProperties), new NetworkCapabilities(this.networkCapabilities), this.network, this.networkMisc != null ? this.networkMisc.subscriberId : null, null);
        }
        return networkState;
    }

    public void lingerRequest(NetworkRequest request, long now, long duration) {
        if (this.mLingerTimerForRequest.get(request.requestId) != null) {
            Log.wtf(TAG, name() + ": request " + request.requestId + " already lingered");
        }
        LingerTimer timer = new LingerTimer(request, now + duration);
        this.mLingerTimers.add(timer);
        this.mLingerTimerForRequest.put(request.requestId, timer);
    }

    public boolean unlingerRequest(NetworkRequest request) {
        LingerTimer timer = (LingerTimer) this.mLingerTimerForRequest.get(request.requestId);
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
        long newExpiry = this.mLingerTimers.isEmpty() ? 0 : ((LingerTimer) this.mLingerTimers.last()).expiryMs;
        if (newExpiry != this.mLingerExpiryMs) {
            if (this.mLingerMessage != null) {
                this.mLingerMessage.cancel();
                this.mLingerMessage = null;
            }
            if (newExpiry > 0) {
                this.mLingerMessage = this.mConnService.makeWakeupMessage(this.mContext, this.mHandler, "NETWORK_LINGER_COMPLETE." + this.network.netId, EVENT_NETWORK_LINGER_COMPLETE, this);
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
        if (this.mLingerMessage != null) {
            this.mLingerMessage.cancel();
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

    public String toString() {
        return "NetworkAgentInfo{ ni{" + this.networkInfo + "}  " + "network{" + this.network + "}  nethandle{" + this.network.getNetworkHandle() + "}  " + "nc{" + this.networkCapabilities + "}  Score{" + getCurrentScore() + "}  " + "everValidated{" + this.everValidated + "}  lastValidated{" + this.lastValidated + "}  " + "created{" + this.created + "} lingering{" + isLingering() + "} " + "explicitlySelected{" + this.networkMisc.explicitlySelected + "} " + "acceptUnvalidated{" + this.networkMisc.acceptUnvalidated + "} " + "everCaptivePortalDetected{" + this.everCaptivePortalDetected + "} " + "lastCaptivePortalDetected{" + this.lastCaptivePortalDetected + "} " + "}";
    }

    public String name() {
        return "NetworkAgentInfo [" + this.networkInfo.getTypeName() + " (" + this.networkInfo.getSubtypeName() + ") - " + (this.network == null ? "null" : this.network.toString()) + "]";
    }

    public int compareTo(NetworkAgentInfo other) {
        return other.getCurrentScore() - getCurrentScore();
    }
}
