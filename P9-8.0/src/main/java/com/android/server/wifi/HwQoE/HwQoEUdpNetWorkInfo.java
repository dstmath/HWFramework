package com.android.server.wifi.HwQoE;

public class HwQoEUdpNetWorkInfo {
    private long mRxUdpBytes;
    private long mRxUdpPackets;
    private long mSumRxPackets;
    private int mSumUdpSockets;
    private int mSumUdpUids;
    private long mTimestamp;
    private long mTxUdpBytes;
    private long mTxUdpPackets;
    private int mUid;
    private int mUidUdpSockets;

    public class UdpNetworkSpeed {
        public long mTimestamp;
        public int mTrafficUid;
        public long mUdpRxSpeed;
        public long mUdpTotalSpeed;
        public long mUdpTxSpeed;

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

    public HwQoEUdpNetWorkInfo(HwQoEUdpNetWorkInfo newInfo) {
        if (newInfo != null) {
            this.mRxUdpBytes = newInfo.getRxUdpBytes();
            this.mRxUdpPackets = newInfo.getRxUdpPackets();
            this.mTxUdpBytes = newInfo.getTxUdpBytes();
            this.mTxUdpPackets = newInfo.getTxUdpPackets();
            this.mUid = newInfo.getUid();
            this.mTimestamp = newInfo.getTimestamp();
            this.mUidUdpSockets = newInfo.getUidUdpSockets();
            this.mSumUdpUids = newInfo.getSumUdpUids();
            this.mSumUdpSockets = newInfo.getSumUdpSockets();
            this.mSumRxPackets = newInfo.getSumRxPackets();
        }
    }

    public void setUdpNetWorkInfo(HwQoEUdpNetWorkInfo newInfo) {
        if (newInfo != null) {
            this.mRxUdpBytes = newInfo.getRxUdpBytes();
            this.mRxUdpPackets = newInfo.getRxUdpPackets();
            this.mTxUdpBytes = newInfo.getTxUdpBytes();
            this.mTxUdpPackets = newInfo.getTxUdpPackets();
            this.mUid = newInfo.getUid();
            this.mTimestamp = newInfo.getTimestamp();
            this.mUidUdpSockets = newInfo.getUidUdpSockets();
            this.mSumUdpUids = newInfo.getSumUdpUids();
            this.mSumUdpSockets = newInfo.getSumUdpSockets();
            this.mSumRxPackets = newInfo.getSumRxPackets();
        }
    }

    public void setRxUdpBytes(long mRxUdpBytes) {
        this.mRxUdpBytes = mRxUdpBytes;
    }

    public long getRxUdpBytes() {
        return this.mRxUdpBytes;
    }

    public void setRxUdpPackets(long mRxUdpPackets) {
        this.mRxUdpPackets = mRxUdpPackets;
    }

    public long getRxUdpPackets() {
        return this.mRxUdpPackets;
    }

    public void setTxUdpBytes(long mTxUdpBytes) {
        this.mTxUdpBytes = mTxUdpBytes;
    }

    public long getTxUdpBytes() {
        return this.mTxUdpBytes;
    }

    public void setTxUdpPackets(long mTxUdpPackets) {
        this.mTxUdpPackets = mTxUdpPackets;
    }

    public long getTxUdpPackets() {
        return this.mTxUdpPackets;
    }

    public void setUid(int mUid) {
        this.mUid = mUid;
    }

    public int getUid() {
        return this.mUid;
    }

    public void setUidUdpSockets(int mUidUdpSockets) {
        this.mUidUdpSockets = mUidUdpSockets;
    }

    public int getUidUdpSockets() {
        return this.mUidUdpSockets;
    }

    public void setSumUdpUids(int mSumUdpUids) {
        this.mSumUdpUids = mSumUdpUids;
    }

    public int getSumUdpUids() {
        return this.mSumUdpUids;
    }

    public void setSumUdpSockets(int mSumUdpSockets) {
        this.mSumUdpSockets = mSumUdpSockets;
    }

    public int getSumUdpSockets() {
        return this.mSumUdpSockets;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public void setTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public long getSumRxPackets() {
        return this.mSumRxPackets;
    }

    public void setSumRxPackets(long mSumRxPackets) {
        this.mSumRxPackets = mSumRxPackets;
    }

    public String dump() {
        StringBuffer buffer = new StringBuffer("UdpNetWorkInfo : ");
        buffer.append("Timestamp: ").append(this.mTimestamp);
        buffer.append(" uid: ").append(this.mUid);
        buffer.append(" mUidUdpSockets: ").append(this.mUidUdpSockets);
        buffer.append(" mRxUdpBytes: ").append(this.mRxUdpBytes);
        buffer.append(" mRxUdpPackets: ").append(this.mRxUdpPackets);
        buffer.append(" mSumRxPackets: ").append(this.mSumRxPackets);
        buffer.append(" mTxUdpBytes: ").append(this.mTxUdpBytes);
        buffer.append(" mTxUdpPackets: ").append(this.mTxUdpPackets);
        buffer.append(" mSumUdpUids: ").append(this.mSumUdpUids);
        buffer.append(" mSumUdpSockets: ").append(this.mSumUdpSockets);
        return buffer.toString();
    }
}
