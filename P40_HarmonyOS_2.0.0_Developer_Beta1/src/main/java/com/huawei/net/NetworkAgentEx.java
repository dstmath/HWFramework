package com.huawei.net;

import android.content.Context;
import android.net.LinkProperties;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Looper;
import com.android.internal.util.Preconditions;

public class NetworkAgentEx {
    private Listener mListener;
    private NetworkAgent mNetworkAgent;

    public interface Listener {
        void unwanted();
    }

    public NetworkAgentEx(Looper looper, Context context, String logTag, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, Listener listener) {
        Preconditions.checkNotNull(looper);
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(ni);
        Preconditions.checkNotNull(nc);
        Preconditions.checkNotNull(lp);
        Preconditions.checkNotNull(listener);
        Preconditions.checkStringNotEmpty(logTag);
        this.mListener = listener;
        this.mNetworkAgent = new NetworkAgent(looper, context, logTag, ni, nc, lp, score) {
            /* class com.huawei.net.NetworkAgentEx.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // android.net.NetworkAgent
            public void unwanted() {
                NetworkAgentEx.this.mListener.unwanted();
            }
        };
    }

    public void sendNetworkCapabilities(NetworkCapabilities nc) {
        if (nc != null) {
            this.mNetworkAgent.sendNetworkCapabilities(nc);
        }
    }

    public void sendNetworkScore(int newScore) {
        this.mNetworkAgent.sendNetworkScore(newScore);
    }

    public void sendNetworkInfo(NetworkInfo networkInfo) {
        if (networkInfo != null) {
            this.mNetworkAgent.sendNetworkInfo(networkInfo);
        }
    }

    public int getNetId() {
        return this.mNetworkAgent.netId;
    }
}
