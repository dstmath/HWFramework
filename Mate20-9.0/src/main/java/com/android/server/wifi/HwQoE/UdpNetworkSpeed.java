package com.android.server.wifi.HwQoE;

public class UdpNetworkSpeed {
    public long mTimestamp;
    public int mTrafficUid;
    public long mUdpRxSpeed;
    public long mUdpTotalSpeed;
    public long mUdpTxSpeed;

    public UdpNetworkSpeed() {
    }

    public UdpNetworkSpeed(UdpNetworkSpeed networkSpeed) {
        if (networkSpeed != null) {
            this.mUdpTxSpeed = networkSpeed.mUdpTxSpeed;
            this.mUdpRxSpeed = networkSpeed.mUdpRxSpeed;
            this.mUdpTotalSpeed = networkSpeed.mUdpTotalSpeed;
            this.mTrafficUid = networkSpeed.mTrafficUid;
            this.mTimestamp = networkSpeed.mTimestamp;
        }
    }

    public void updateUdpSpeed(long txSpeed, long rxSpeed, int uid) {
        this.mUdpTxSpeed = txSpeed;
        this.mUdpRxSpeed = rxSpeed;
        this.mTrafficUid = uid;
        this.mUdpTotalSpeed = this.mUdpRxSpeed + this.mUdpTxSpeed;
        this.mTimestamp = System.currentTimeMillis();
    }
}
