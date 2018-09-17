package com.android.server.connectivity;

import android.content.Context;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkMisc;
import android.net.NetworkRequest;
import android.net.NetworkState;
import android.os.Handler;
import android.os.Messenger;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import com.android.server.ConnectivityService;
import com.android.server.HwServiceFactory;
import java.util.ArrayList;

public class NetworkAgentInfo implements Comparable<NetworkAgentInfo> {
    private static final int MAXIMUM_NETWORK_SCORE = 100;
    private static final int UNVALIDATED_SCORE_PENALTY = 40;
    public final AsyncChannel asyncChannel;
    public Nat464Xlat clatd;
    public boolean created;
    private int currentScore;
    public boolean everCaptivePortalDetected;
    public boolean everConnected;
    public boolean everValidated;
    public boolean lastCaptivePortalDetected;
    public boolean lastValidated;
    public boolean lingering;
    public LinkProperties linkProperties;
    public final Messenger messenger;
    public final Network network;
    public NetworkCapabilities networkCapabilities;
    public NetworkInfo networkInfo;
    public final ArrayList<NetworkRequest> networkLingered;
    public final NetworkMisc networkMisc;
    public final NetworkMonitor networkMonitor;
    public final SparseArray<NetworkRequest> networkRequests;

    public NetworkAgentInfo(Messenger messenger, AsyncChannel ac, Network net, NetworkInfo info, LinkProperties lp, NetworkCapabilities nc, int score, Context context, Handler handler, NetworkMisc misc, NetworkRequest defaultRequest, ConnectivityService connService) {
        this.networkRequests = new SparseArray();
        this.networkLingered = new ArrayList();
        this.messenger = messenger;
        this.asyncChannel = ac;
        this.network = net;
        this.networkInfo = info;
        this.linkProperties = lp;
        this.networkCapabilities = nc;
        this.currentScore = score;
        this.networkMonitor = HwServiceFactory.getHwConnectivityManager().createHwNetworkMonitor(context, handler, this, defaultRequest);
        this.networkMisc = misc;
    }

    public boolean addRequest(NetworkRequest networkRequest) {
        if (this.networkRequests.get(networkRequest.requestId) == networkRequest) {
            return false;
        }
        this.networkRequests.put(networkRequest.requestId, networkRequest);
        return true;
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

    private int getCurrentScore(boolean pretendValidated) {
        if (this.networkMisc.explicitlySelected && (this.networkMisc.acceptUnvalidated || pretendValidated)) {
            return MAXIMUM_NETWORK_SCORE;
        }
        int score = this.currentScore;
        if (!(this.networkCapabilities.hasCapability(16) || pretendValidated)) {
            score -= 40;
        }
        if (score < 0) {
            score = 0;
        }
        if (this.networkMisc != null && this.networkMisc.explicitlySelected) {
            score = MAXIMUM_NETWORK_SCORE;
        }
        return score;
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

    public String toString() {
        return "NetworkAgentInfo{ ni{" + this.networkInfo + "}  " + "network{" + this.network + "}  nethandle{" + this.network.getNetworkHandle() + "}  " + "nc{" + this.networkCapabilities + "}  Score{" + getCurrentScore() + "}  " + "everValidated{" + this.everValidated + "}  lastValidated{" + this.lastValidated + "}  " + "created{" + this.created + "} lingering{" + this.lingering + "} " + "explicitlySelected{" + this.networkMisc.explicitlySelected + "} " + "acceptUnvalidated{" + this.networkMisc.acceptUnvalidated + "} " + "everCaptivePortalDetected{" + this.everCaptivePortalDetected + "} " + "lastCaptivePortalDetected{" + this.lastCaptivePortalDetected + "} " + "}";
    }

    public String name() {
        return "NetworkAgentInfo [" + this.networkInfo.getTypeName() + " (" + this.networkInfo.getSubtypeName() + ") - " + (this.network == null ? "null" : this.network.toString()) + "]";
    }

    public int compareTo(NetworkAgentInfo other) {
        return other.getCurrentScore() - getCurrentScore();
    }
}
