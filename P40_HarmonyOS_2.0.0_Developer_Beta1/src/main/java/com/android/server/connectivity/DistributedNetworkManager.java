package com.android.server.connectivity;

import android.content.Context;
import android.util.Log;
import com.huawei.distributed.net.DistributedNetAgent;
import com.huawei.distributed.net.DistributedNetSettings;
import com.huawei.distributed.net.DistributedNetworkConstants;
import com.huawei.net.NetworkAgentEx;

public class DistributedNetworkManager implements DistributedNetSettings.Listener {
    private static final String TAG = "DistributedNetworkManager";
    private final Context mContext;
    private boolean mIsEnabled;
    private boolean mIsMetered;
    private String mNetName;
    private final DistributedNetAgent mNetworkAgent;
    private final DistributedNetSettings mNetworkSettings;

    public DistributedNetworkManager(Context context) {
        if (context != null) {
            this.mContext = context;
            this.mNetworkAgent = new DistributedNetAgent(this.mContext);
            this.mNetworkSettings = new DistributedNetSettings(this.mContext, this);
            this.mIsEnabled = false;
            this.mIsMetered = false;
            this.mNetworkSettings.resetDistributedNetSettings();
            Log.i(TAG, "DistributedNetworkManager construction completed.");
            return;
        }
        throw new IllegalArgumentException("context is null.");
    }

    public void startDistributedNetwork(String reason, String networkName, int netScore, NetworkAgentEx.Listener listener) {
        Log.i(TAG, "Start distributed network , reason " + reason);
        this.mNetName = networkName;
        this.mNetworkAgent.setupDistributedNetwork(reason, networkName, netScore, listener);
        this.mNetworkAgent.setNetConnected(reason, networkName);
    }

    public void stopDistributedNetwork(String reason) {
        Log.i(TAG, "Stop distributed network, reason " + reason);
        this.mNetworkAgent.setNetDisconneted(reason, this.mNetName);
    }

    public void setNetworkScore(int newScore) {
        Log.i(TAG, "Set distributed network score to " + newScore);
        this.mNetworkAgent.setNetScore(newScore);
    }

    public int getNetworkScore() {
        return this.mNetworkAgent.getNetScore();
    }

    @Override // com.huawei.distributed.net.DistributedNetSettings.Listener
    public void onNetEnabledChanged(boolean isEnabled) {
        if (this.mIsEnabled == isEnabled) {
            Log.i(TAG, "onNetEnabledChanged, found no change from current, return.");
            return;
        }
        this.mIsEnabled = isEnabled;
        if (isEnabled) {
            startDistributedNetwork(DistributedNetworkConstants.REASON_CONNECTED, DistributedNetworkConstants.NETWORK_NAME, DistributedNetworkConstants.getNetworkScoreWithChargingState(this.mNetworkSettings.isCharging()), new NetworkAgentEx.Listener() {
                /* class com.android.server.connectivity.$$Lambda$DistributedNetworkManager$I1gTam2JUrB41Msjhy3DDGj8Ck */

                public final void unwanted() {
                    DistributedNetworkManager.this.lambda$onNetEnabledChanged$0$DistributedNetworkManager();
                }
            });
            this.mIsMetered = this.mNetworkSettings.isDistributedNetMetered();
            this.mNetworkAgent.setMetered(this.mIsMetered);
        } else {
            stopDistributedNetwork(DistributedNetworkConstants.REASON_DISCONNECTED);
        }
        Log.i(TAG, "onNetEnabledChanged, enabled : " + isEnabled + ", metered : " + this.mIsMetered);
    }

    public /* synthetic */ void lambda$onNetEnabledChanged$0$DistributedNetworkManager() {
        stopDistributedNetwork(DistributedNetworkConstants.REASON_UNWANTED);
    }

    @Override // com.huawei.distributed.net.DistributedNetSettings.Listener
    public void onNetMeteredChanged(boolean isMetered) {
        if (this.mIsMetered != isMetered) {
            this.mIsMetered = isMetered;
            this.mNetworkAgent.setMetered(this.mIsMetered);
            Log.i(TAG, "onNetMeteredChanged, metered : " + this.mIsMetered);
        }
    }

    @Override // com.huawei.distributed.net.DistributedNetSettings.Listener
    public void onDeviceChargingChanged(boolean isCharging) {
        setNetworkScore(DistributedNetworkConstants.getNetworkScoreWithChargingState(isCharging));
    }
}
