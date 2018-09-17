package com.android.server.connectivity.tethering;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.net.NetworkState;
import android.net.util.SharedLog;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.android.internal.util.StateMachine;
import java.util.HashMap;

public class UpstreamNetworkMonitor {
    private static final int CALLBACK_LISTEN_ALL = 1;
    private static final int CALLBACK_MOBILE_REQUEST = 3;
    private static final int CALLBACK_TRACK_DEFAULT = 2;
    private static final boolean DBG = false;
    public static final int EVENT_ON_AVAILABLE = 1;
    public static final int EVENT_ON_CAPABILITIES = 2;
    public static final int EVENT_ON_LINKPROPERTIES = 3;
    public static final int EVENT_ON_LOST = 4;
    private static final String TAG = UpstreamNetworkMonitor.class.getSimpleName();
    private static final boolean VDBG = false;
    private ConnectivityManager mCM;
    private final Context mContext;
    private Network mCurrentDefault;
    private NetworkCallback mDefaultNetworkCallback;
    private boolean mDunRequired;
    private final Handler mHandler;
    private NetworkCallback mListenAllCallback;
    private final SharedLog mLog;
    private NetworkCallback mMobileNetworkCallback;
    private final HashMap<Network, NetworkState> mNetworkMap;
    private final StateMachine mTarget;
    private final int mWhat;

    private class UpstreamNetworkCallback extends NetworkCallback {
        private final int mCallbackType;

        UpstreamNetworkCallback(int callbackType) {
            this.mCallbackType = callbackType;
        }

        public void onAvailable(Network network) {
            checkExpectedThread();
            UpstreamNetworkMonitor.this.handleAvailable(this.mCallbackType, network);
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities newNc) {
            checkExpectedThread();
            UpstreamNetworkMonitor.this.handleNetCap(network, newNc);
        }

        public void onLinkPropertiesChanged(Network network, LinkProperties newLp) {
            checkExpectedThread();
            UpstreamNetworkMonitor.this.handleLinkProp(network, newLp);
        }

        public void onLost(Network network) {
            checkExpectedThread();
            UpstreamNetworkMonitor.this.handleLost(this.mCallbackType, network);
        }

        private void checkExpectedThread() {
            if (Looper.myLooper() != UpstreamNetworkMonitor.this.mHandler.getLooper()) {
                Log.wtf(UpstreamNetworkMonitor.TAG, "Handling callback in unexpected thread.");
            }
        }
    }

    public UpstreamNetworkMonitor(Context ctx, StateMachine tgt, int what, SharedLog log) {
        this.mNetworkMap = new HashMap();
        this.mContext = ctx;
        this.mTarget = tgt;
        this.mHandler = this.mTarget.getHandler();
        this.mWhat = what;
        this.mLog = log.forSubComponent(TAG);
    }

    public UpstreamNetworkMonitor(StateMachine tgt, int what, ConnectivityManager cm, SharedLog log) {
        this(null, tgt, what, log);
        this.mCM = cm;
    }

    public void start() {
        stop();
        NetworkRequest listenAllRequest = new Builder().clearCapabilities().build();
        this.mListenAllCallback = new UpstreamNetworkCallback(1);
        cm().registerNetworkCallback(listenAllRequest, this.mListenAllCallback, this.mHandler);
        this.mDefaultNetworkCallback = new UpstreamNetworkCallback(2);
        cm().registerDefaultNetworkCallback(this.mDefaultNetworkCallback, this.mHandler);
    }

    public void stop() {
        releaseMobileNetworkRequest();
        releaseCallback(this.mDefaultNetworkCallback);
        this.mDefaultNetworkCallback = null;
        releaseCallback(this.mListenAllCallback);
        this.mListenAllCallback = null;
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
            return;
        }
        int legacyType = this.mDunRequired ? 4 : 5;
        NetworkRequest mobileUpstreamRequest = new Builder().setCapabilities(ConnectivityManager.networkCapabilitiesForType(legacyType)).build();
        this.mMobileNetworkCallback = new UpstreamNetworkCallback(3);
        this.mLog.i("requesting mobile upstream network: " + mobileUpstreamRequest);
        cm().requestNetwork(mobileUpstreamRequest, this.mMobileNetworkCallback, 0, legacyType, this.mHandler);
    }

    public void releaseMobileNetworkRequest() {
        if (this.mMobileNetworkCallback != null) {
            cm().unregisterNetworkCallback(this.mMobileNetworkCallback);
            this.mMobileNetworkCallback = null;
        }
    }

    public NetworkState lookup(Network network) {
        return network != null ? (NetworkState) this.mNetworkMap.get(network) : null;
    }

    private void handleAvailable(int callbackType, Network network) {
        if (!this.mNetworkMap.containsKey(network)) {
            this.mNetworkMap.put(network, new NetworkState(null, null, null, network, null, null));
        }
        switch (callbackType) {
            case 2:
                if (this.mDefaultNetworkCallback != null) {
                    this.mCurrentDefault = network;
                    break;
                }
                return;
            case 3:
                if (this.mMobileNetworkCallback == null) {
                    return;
                }
                break;
        }
        notifyTarget(1, network);
    }

    private void handleNetCap(Network network, NetworkCapabilities newNc) {
        NetworkState prev = (NetworkState) this.mNetworkMap.get(network);
        if (prev != null && !newNc.equals(prev.networkCapabilities)) {
            this.mNetworkMap.put(network, new NetworkState(null, prev.linkProperties, newNc, network, null, null));
            notifyTarget(2, network);
        }
    }

    private void handleLinkProp(Network network, LinkProperties newLp) {
        NetworkState prev = (NetworkState) this.mNetworkMap.get(network);
        if (prev != null && !newLp.equals(prev.linkProperties)) {
            this.mNetworkMap.put(network, new NetworkState(null, newLp, prev.networkCapabilities, network, null, null));
            notifyTarget(3, network);
        }
    }

    private void handleLost(int callbackType, Network network) {
        if (callbackType == 2) {
            this.mCurrentDefault = null;
        } else if (this.mNetworkMap.containsKey(network)) {
            notifyTarget(4, (NetworkState) this.mNetworkMap.remove(network));
        }
    }

    private ConnectivityManager cm() {
        if (this.mCM == null) {
            this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
        return this.mCM;
    }

    private void releaseCallback(NetworkCallback cb) {
        if (cb != null) {
            cm().unregisterNetworkCallback(cb);
        }
    }

    private void notifyTarget(int which, Network network) {
        notifyTarget(which, (NetworkState) this.mNetworkMap.get(network));
    }

    private void notifyTarget(int which, NetworkState netstate) {
        this.mTarget.sendMessage(this.mWhat, which, 0, netstate);
    }
}
