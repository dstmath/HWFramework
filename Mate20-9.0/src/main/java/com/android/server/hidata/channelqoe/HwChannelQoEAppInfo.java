package com.android.server.hidata.channelqoe;

public class HwChannelQoEAppInfo {
    public IChannelQoECallback callback;
    public int good_times = 0;
    public int mNetwork;
    public int mQci;
    public int mScence;
    public int mUID;

    public HwChannelQoEAppInfo(int uid, int scence, int network, int qci, IChannelQoECallback icallback) {
        this.mUID = uid;
        this.mScence = scence;
        this.mNetwork = network;
        this.callback = icallback;
        this.mQci = qci;
    }

    public int getTput() {
        return HwCHQciManager.getInstance().getChQciConfig(this.mQci).mTput;
    }
}
