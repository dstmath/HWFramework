package com.huawei.distributed.net;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.Looper;
import android.util.Log;
import com.huawei.android.net.NetworkCapabilitiesEx;
import com.huawei.net.NetworkAgentEx;
import com.huawei.net.NetworkInfoEx;

public class DistributedNetAgent {
    private static final int INVALID_SCORE = 0;
    private static final NetworkInfo ORIGIN_NET_INFO = NetworkInfoEx.makeNetworkInfo(0, 0, DistributedNetworkConstants.NET_TYPE_NAME, DistributedNetworkConstants.SUBTYPE_NAME_BLUETOOTH);
    private static final String TAG = "DistributedNetAgent";
    private NetworkCapabilitiesEx mCapabilities;
    private final Context mContext;
    private final DistributedNetLinkProperties mLinkProperties;
    private NetworkAgentEx mNetworkAgent;
    private NetworkInfo mNetworkInfo;
    private int mNetworkScore;

    public DistributedNetAgent(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mCapabilities = new NetworkCapabilitiesEx();
            this.mLinkProperties = new DistributedNetLinkProperties();
            buildNetworkCapabilities();
            return;
        }
        throw new IllegalArgumentException("context is null");
    }

    private void buildNetworkCapabilities() {
        this.mCapabilities.addTransportType(2);
        this.mCapabilities.addCapability(12);
        this.mCapabilities.addCapability(13);
        this.mCapabilities.addCapability(18);
        this.mCapabilities.addCapability(20);
        this.mCapabilities.removeCapability(11);
        this.mCapabilities.setLinkUpstreamBandwidthKbps((int) DistributedNetworkConstants.BANDWIDTH_KBPS_BLUETOOTH);
        this.mCapabilities.setLinkDownstreamBandwidthKbps((int) DistributedNetworkConstants.BANDWIDTH_KBPS_BLUETOOTH);
    }

    public void setupDistributedNetwork(String reason, String companionDeviceName, int netScore, NetworkAgentEx.Listener listener) {
        if (listener == null) {
            Log.e(TAG, "Failed to setup distributed network because callback is null.");
        }
        if (this.mNetworkAgent != null) {
            Log.e(TAG, "Already exists a network agent " + this.mNetworkAgent.getNetId() + ", RETURN.");
            return;
        }
        this.mNetworkInfo = NetworkInfoEx.makeNetworkInfo(ORIGIN_NET_INFO);
        NetworkInfoEx.setIsAvailable(this.mNetworkInfo, true);
        NetworkInfoEx.setDetailedState(this.mNetworkInfo, NetworkInfo.DetailedState.CONNECTING, reason, companionDeviceName);
        this.mNetworkScore = netScore;
        this.mNetworkAgent = new NetworkAgentEx(Looper.getMainLooper(), this.mContext, TAG, this.mNetworkInfo, this.mCapabilities.getNetworkCapabilities(), this.mLinkProperties.getLinkProperties(), netScore, listener);
    }

    public void setNetConnected(String reason, String companionDeviceName) {
        updateNetworkInfo(NetworkInfo.DetailedState.CONNECTED, reason, companionDeviceName);
    }

    public void setNetDisconneted(String reason, String comanionDeviceName) {
        updateNetworkInfo(NetworkInfo.DetailedState.DISCONNECTED, reason, comanionDeviceName);
        this.mNetworkAgent = null;
        this.mNetworkInfo = null;
    }

    public int getNetScore() {
        if (this.mNetworkAgent != null) {
            return this.mNetworkScore;
        }
        return 0;
    }

    public void setNetScore(int newScore) {
        if (this.mNetworkScore != newScore && this.mNetworkAgent != null) {
            Log.i(TAG, "update network score from " + this.mNetworkScore + " to " + newScore);
            this.mNetworkScore = newScore;
            this.mNetworkAgent.sendNetworkScore(this.mNetworkScore);
        }
    }

    public void updateNetworkCapabilities(NetworkCapabilitiesEx newCapabilities) {
        NetworkAgentEx networkAgentEx = this.mNetworkAgent;
        if (networkAgentEx != null) {
            this.mCapabilities = newCapabilities;
            networkAgentEx.sendNetworkCapabilities(this.mCapabilities.getNetworkCapabilities());
        }
    }

    public void setMetered(boolean isMetered) {
        if (isMetered) {
            this.mCapabilities.removeCapability(11);
        } else {
            this.mCapabilities.addCapability(11);
        }
        NetworkAgentEx networkAgentEx = this.mNetworkAgent;
        if (networkAgentEx != null) {
            networkAgentEx.sendNetworkCapabilities(this.mCapabilities.getNetworkCapabilities());
        }
    }

    private void updateNetworkInfo(NetworkInfo.DetailedState state, String reason, String extraInfo) {
        if (this.mNetworkAgent == null || this.mNetworkInfo == null) {
            Log.e(TAG, "Failed to update network info on a null network agent.");
            return;
        }
        Log.i(TAG, "update network to state " + state + ", reason " + reason);
        NetworkInfoEx.setDetailedState(this.mNetworkInfo, state, reason, extraInfo);
        this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
    }
}
