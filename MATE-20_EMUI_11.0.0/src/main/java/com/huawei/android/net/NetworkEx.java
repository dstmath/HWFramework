package com.huawei.android.net;

import android.net.Network;

public class NetworkEx {
    private Network mNetwork;

    public NetworkEx(Network network) {
        this.mNetwork = new Network(network);
    }

    public int getNetId() {
        Network network = this.mNetwork;
        if (network == null) {
            return 0;
        }
        return network.netId;
    }
}
