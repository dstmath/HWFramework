package com.android.server.hidata;

public class HwQoEUdpNetWorkInfo {
    public int mNetworkID;
    public long mRxTcpBytes = 0;
    public long mRxTcpPackets = 0;
    private long mRxUdpBytes;
    private long mRxUdpPackets;
    private long mSumRxPackets;
    private int mSumUdpSockets;
    private int mSumUdpUids;
    private long mTimestamp;
    public long mTxTcpBytes = 0;
    public long mTxTcpPackets = 0;
    private long mTxUdpBytes;
    private long mTxUdpPackets;
    private int mUid;
    private int mUidUdpSockets;

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
            this.mRxTcpBytes = newInfo.mRxTcpBytes;
            this.mRxTcpPackets = newInfo.mRxTcpPackets;
            this.mTxTcpBytes = newInfo.mTxTcpBytes;
            this.mTxTcpPackets = newInfo.mTxTcpPackets;
            this.mNetworkID = newInfo.mNetworkID;
        }
    }

    public void setRxUdpBytes(long mRxUdpBytes2) {
        this.mRxUdpBytes = mRxUdpBytes2;
    }

    public long getRxUdpBytes() {
        return this.mRxUdpBytes;
    }

    public void setRxUdpPackets(long mRxUdpPackets2) {
        this.mRxUdpPackets = mRxUdpPackets2;
    }

    public long getRxUdpPackets() {
        return this.mRxUdpPackets;
    }

    public void setTxUdpBytes(long mTxUdpBytes2) {
        this.mTxUdpBytes = mTxUdpBytes2;
    }

    public long getTxUdpBytes() {
        return this.mTxUdpBytes;
    }

    public void setTxUdpPackets(long mTxUdpPackets2) {
        this.mTxUdpPackets = mTxUdpPackets2;
    }

    public long getTxUdpPackets() {
        return this.mTxUdpPackets;
    }

    public void setUid(int mUid2) {
        this.mUid = mUid2;
    }

    public int getUid() {
        return this.mUid;
    }

    public void setUidUdpSockets(int mUidUdpSockets2) {
        this.mUidUdpSockets = mUidUdpSockets2;
    }

    public int getUidUdpSockets() {
        return this.mUidUdpSockets;
    }

    public void setSumUdpUids(int mSumUdpUids2) {
        this.mSumUdpUids = mSumUdpUids2;
    }

    public int getSumUdpUids() {
        return this.mSumUdpUids;
    }

    public void setSumUdpSockets(int mSumUdpSockets2) {
        this.mSumUdpSockets = mSumUdpSockets2;
    }

    public int getSumUdpSockets() {
        return this.mSumUdpSockets;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public void setTimestamp(long mTimestamp2) {
        this.mTimestamp = mTimestamp2;
    }

    public long getSumRxPackets() {
        return this.mSumRxPackets;
    }

    public void setSumRxPackets(long mSumRxPackets2) {
        this.mSumRxPackets = mSumRxPackets2;
    }

    public void setNetworkID(int mNetworkID2) {
        this.mNetworkID = mNetworkID2;
    }

    public int getNetwork() {
        return this.mNetworkID;
    }

    public String dump() {
        StringBuffer buffer = new StringBuffer("UdpNetWorkInfo : ");
        buffer.append("Timestamp: ");
        buffer.append(this.mTimestamp);
        buffer.append(" uid: ");
        buffer.append(this.mUid);
        buffer.append(" mUidUdpSockets: ");
        buffer.append(this.mUidUdpSockets);
        buffer.append(" mRxUdpBytes: ");
        buffer.append(this.mRxUdpBytes);
        buffer.append(" mRxUdpPackets: ");
        buffer.append(this.mRxUdpPackets);
        buffer.append(" mSumRxPackets: ");
        buffer.append(this.mSumRxPackets);
        buffer.append(" mTxUdpBytes: ");
        buffer.append(this.mTxUdpBytes);
        buffer.append(" mTxUdpPackets: ");
        buffer.append(this.mTxUdpPackets);
        buffer.append(" mSumUdpUids: ");
        buffer.append(this.mSumUdpUids);
        buffer.append(" mSumUdpSockets: ");
        buffer.append(this.mSumUdpSockets);
        buffer.append(" mNetworkID: ");
        buffer.append(this.mNetworkID);
        return buffer.toString();
    }
}
