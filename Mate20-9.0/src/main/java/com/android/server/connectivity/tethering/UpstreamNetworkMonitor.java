package com.android.server.connectivity.tethering;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.IpPrefix;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkState;
import android.net.util.PrefixUtils;
import android.net.util.SharedLog;
import android.os.Handler;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.StateMachine;
import com.android.server.UiModeManagerService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class UpstreamNetworkMonitor {
    private static final int CALLBACK_LISTEN_ALL = 1;
    private static final int CALLBACK_MOBILE_REQUEST = 3;
    private static final int CALLBACK_TRACK_DEFAULT = 2;
    private static final boolean DBG = false;
    public static final int EVENT_ON_AVAILABLE = 1;
    public static final int EVENT_ON_CAPABILITIES = 2;
    public static final int EVENT_ON_LINKPROPERTIES = 3;
    public static final int EVENT_ON_LOST = 4;
    public static final int NOTIFY_LOCAL_PREFIXES = 10;
    private static final String TAG = UpstreamNetworkMonitor.class.getSimpleName();
    private static final boolean VDBG = false;
    private ConnectivityManager mCM;
    private final Context mContext;
    private Network mDefaultInternetNetwork;
    private ConnectivityManager.NetworkCallback mDefaultNetworkCallback;
    private boolean mDunRequired;
    private final Handler mHandler;
    private ConnectivityManager.NetworkCallback mListenAllCallback;
    private HashSet<IpPrefix> mLocalPrefixes;
    private final SharedLog mLog;
    private ConnectivityManager.NetworkCallback mMobileNetworkCallback;
    private final HashMap<Network, NetworkState> mNetworkMap;
    private final StateMachine mTarget;
    private Network mTetheringUpstreamNetwork;
    private final int mWhat;

    private static class TypeStatePair {
        public NetworkState ns;
        public int type;

        private TypeStatePair() {
            this.type = -1;
            this.ns = null;
        }
    }

    private class UpstreamNetworkCallback extends ConnectivityManager.NetworkCallback {
        private final int mCallbackType;

        UpstreamNetworkCallback(int callbackType) {
            this.mCallbackType = callbackType;
        }

        public void onAvailable(Network network) {
            UpstreamNetworkMonitor.this.handleAvailable(this.mCallbackType, network);
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities newNc) {
            UpstreamNetworkMonitor.this.handleNetCap(network, newNc);
        }

        public void onLinkPropertiesChanged(Network network, LinkProperties newLp) {
            UpstreamNetworkMonitor.this.handleLinkProp(network, newLp);
            UpstreamNetworkMonitor.this.recomputeLocalPrefixes();
        }

        public void onNetworkSuspended(Network network) {
            UpstreamNetworkMonitor.this.handleSuspended(this.mCallbackType, network);
        }

        public void onNetworkResumed(Network network) {
            UpstreamNetworkMonitor.this.handleResumed(this.mCallbackType, network);
        }

        public void onLost(Network network) {
            UpstreamNetworkMonitor.this.handleLost(this.mCallbackType, network);
            UpstreamNetworkMonitor.this.recomputeLocalPrefixes();
        }
    }

    public UpstreamNetworkMonitor(Context ctx, StateMachine tgt, SharedLog log, int what) {
        this.mNetworkMap = new HashMap<>();
        this.mContext = ctx;
        this.mTarget = tgt;
        this.mHandler = this.mTarget.getHandler();
        this.mLog = log.forSubComponent(TAG);
        this.mWhat = what;
        this.mLocalPrefixes = new HashSet<>();
    }

    @VisibleForTesting
    public UpstreamNetworkMonitor(ConnectivityManager cm, StateMachine tgt, SharedLog log, int what) {
        this((Context) null, tgt, log, what);
        this.mCM = cm;
    }

    public void start() {
        stop();
        NetworkRequest listenAllRequest = new NetworkRequest.Builder().clearCapabilities().build();
        this.mListenAllCallback = new UpstreamNetworkCallback(1);
        cm().registerNetworkCallback(listenAllRequest, this.mListenAllCallback, this.mHandler);
        this.mDefaultNetworkCallback = new UpstreamNetworkCallback(2);
        cm().registerDefaultNetworkCallback(this.mDefaultNetworkCallback, this.mHandler);
    }

    public void stop() {
        releaseMobileNetworkRequest();
        releaseCallback(this.mDefaultNetworkCallback);
        this.mDefaultNetworkCallback = null;
        this.mDefaultInternetNetwork = null;
        releaseCallback(this.mListenAllCallback);
        this.mListenAllCallback = null;
        this.mTetheringUpstreamNetwork = null;
        this.mNetworkMap.clear();
    }

    public void updateMobileRequiresDun(boolean dunRequired) {
        boolean valueChanged = this.mDunRequired != dunRequired;
        this.mDunRequired = dunRequired;
        if (valueChanged && mobileNetworkRequested()) {
            releaseMobileNetworkRequest();
            registerMobileNetworkRequest();
        }
    }

    public boolean mobileNetworkRequested() {
        return this.mMobileNetworkCallback != null;
    }

    public void registerMobileNetworkRequest() {
        if (this.mMobileNetworkCallback != null) {
            this.mLog.e("registerMobileNetworkRequest() already registered");
        } else if (!SystemProperties.getBoolean("sys.defaultapn.enabled", true)) {
            Log.d(TAG, "registerMobileNetworkRequest abort for defaultMobileEnable is false.");
        } else {
            int legacyType = this.mDunRequired ? 4 : 5;
            NetworkRequest mobileUpstreamRequest = new NetworkRequest.Builder().setCapabilities(ConnectivityManager.networkCapabilitiesForType(legacyType)).build();
            this.mMobileNetworkCallback = new UpstreamNetworkCallback(3);
            SharedLog sharedLog = this.mLog;
            sharedLog.i("requesting mobile upstream network: " + mobileUpstreamRequest);
            cm().requestNetwork(mobileUpstreamRequest, this.mMobileNetworkCallback, 0, legacyType, this.mHandler);
        }
    }

    public void releaseMobileNetworkRequest() {
        if (this.mMobileNetworkCallback != null) {
            cm().unregisterNetworkCallback(this.mMobileNetworkCallback);
            this.mMobileNetworkCallback = null;
        }
    }

    public NetworkState selectPreferredUpstreamType(Iterable<Integer> preferredTypes) {
        TypeStatePair typeStatePair = findFirstAvailableUpstreamByType(this.mNetworkMap.values(), preferredTypes);
        SharedLog sharedLog = this.mLog;
        sharedLog.log("preferred upstream type: " + ConnectivityManager.getNetworkTypeName(typeStatePair.type));
        int i = typeStatePair.type;
        if (i != -1) {
            switch (i) {
                case 4:
                case 5:
                    registerMobileNetworkRequest();
                    break;
                default:
                    releaseMobileNetworkRequest();
                    break;
            }
        }
        return typeStatePair.ns;
    }

    public void setCurrentUpstream(Network upstream) {
        this.mTetheringUpstreamNetwork = upstream;
    }

    public Set<IpPrefix> getLocalPrefixes() {
        return (Set) this.mLocalPrefixes.clone();
    }

    /* access modifiers changed from: private */
    public void handleAvailable(int callbackType, Network network) {
        if (!this.mNetworkMap.containsKey(network)) {
            HashMap<Network, NetworkState> hashMap = this.mNetworkMap;
            NetworkState networkState = new NetworkState(null, null, null, network, null, null);
            hashMap.put(network, networkState);
        }
        switch (callbackType) {
            case 2:
                if (this.mDefaultNetworkCallback != null) {
                    this.mDefaultInternetNetwork = network;
                    break;
                } else {
                    return;
                }
            case 3:
                if (this.mMobileNetworkCallback == null) {
                    return;
                }
                break;
        }
        notifyTarget(1, network);
    }

    /* access modifiers changed from: private */
    public void handleNetCap(Network network, NetworkCapabilities newNc) {
        NetworkState prev = this.mNetworkMap.get(network);
        if (prev != null && !newNc.equals(prev.networkCapabilities)) {
            if (network.equals(this.mTetheringUpstreamNetwork) && newNc.hasSignalStrength()) {
                int newSignal = newNc.getSignalStrength();
                String prevSignal = getSignalStrength(prev.networkCapabilities);
                this.mLog.logf("upstream network signal strength: %s -> %s", prevSignal, Integer.valueOf(newSignal));
            }
            HashMap<Network, NetworkState> hashMap = this.mNetworkMap;
            NetworkState networkState = new NetworkState(null, prev.linkProperties, newNc, network, null, null);
            hashMap.put(network, networkState);
            notifyTarget(2, network);
        }
    }

    /* access modifiers changed from: private */
    public void handleLinkProp(Network network, LinkProperties newLp) {
        NetworkState prev = this.mNetworkMap.get(network);
        if (prev != null && !newLp.equals(prev.linkProperties)) {
            HashMap<Network, NetworkState> hashMap = this.mNetworkMap;
            NetworkState networkState = new NetworkState(null, newLp, prev.networkCapabilities, network, null, null);
            hashMap.put(network, networkState);
            notifyTarget(3, network);
        }
    }

    /* access modifiers changed from: private */
    public void handleSuspended(int callbackType, Network network) {
        if (callbackType == 1 && network.equals(this.mTetheringUpstreamNetwork)) {
            SharedLog sharedLog = this.mLog;
            sharedLog.log("SUSPENDED current upstream: " + network);
        }
    }

    /* access modifiers changed from: private */
    public void handleResumed(int callbackType, Network network) {
        if (callbackType == 1 && network.equals(this.mTetheringUpstreamNetwork)) {
            SharedLog sharedLog = this.mLog;
            sharedLog.log("RESUMED current upstream: " + network);
        }
    }

    /* access modifiers changed from: private */
    public void handleLost(int callbackType, Network network) {
        if (callbackType == 2) {
            this.mDefaultInternetNetwork = null;
        } else if (this.mNetworkMap.containsKey(network)) {
            notifyTarget(4, (Object) this.mNetworkMap.remove(network));
        }
    }

    /* access modifiers changed from: private */
    public void recomputeLocalPrefixes() {
        HashSet<IpPrefix> localPrefixes = allLocalPrefixes(this.mNetworkMap.values());
        if (!this.mLocalPrefixes.equals(localPrefixes)) {
            this.mLocalPrefixes = localPrefixes;
            notifyTarget(10, localPrefixes.clone());
        }
    }

    private ConnectivityManager cm() {
        if (this.mCM == null) {
            this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        return this.mCM;
    }

    private void releaseCallback(ConnectivityManager.NetworkCallback cb) {
        if (cb != null) {
            cm().unregisterNetworkCallback(cb);
        }
    }

    private void notifyTarget(int which, Network network) {
        notifyTarget(which, (Object) this.mNetworkMap.get(network));
    }

    private void notifyTarget(int which, Object obj) {
        this.mTarget.sendMessage(this.mWhat, which, 0, obj);
    }

    private static TypeStatePair findFirstAvailableUpstreamByType(Iterable<NetworkState> netStates, Iterable<Integer> preferredTypes) {
        TypeStatePair result = new TypeStatePair();
        for (Integer intValue : preferredTypes) {
            int type = intValue.intValue();
            try {
                NetworkCapabilities nc = ConnectivityManager.networkCapabilitiesForType(type);
                nc.setSingleUid(Process.myUid());
                for (NetworkState value : netStates) {
                    if (nc.satisfiedByNetworkCapabilities(value.networkCapabilities)) {
                        result.type = type;
                        result.ns = value;
                        return result;
                    }
                }
                continue;
            } catch (IllegalArgumentException e) {
                String str = TAG;
                Log.e(str, "No NetworkCapabilities mapping for legacy type: " + ConnectivityManager.getNetworkTypeName(type));
            }
        }
        return result;
    }

    private static HashSet<IpPrefix> allLocalPrefixes(Iterable<NetworkState> netStates) {
        HashSet<IpPrefix> prefixSet = new HashSet<>();
        for (NetworkState ns : netStates) {
            LinkProperties lp = ns.linkProperties;
            if (lp != null) {
                prefixSet.addAll(PrefixUtils.localPrefixesFrom(lp));
            }
        }
        return prefixSet;
    }

    private static String getSignalStrength(NetworkCapabilities nc) {
        if (nc == null || !nc.hasSignalStrength()) {
            return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
        }
        return Integer.toString(nc.getSignalStrength());
    }
}
