package com.android.server.hidata.histream;

public class HwHistreamCHRQoeInfo {
    public int mDlTup = -1;
    public int mNetDlTup = -1;
    public int mSceneId = -1;
    public int mUlTup = -1;
    public int mVideoQoe = -1;

    public HwHistreamCHRQoeInfo(int sceneId, int videoQoe, int ulTup, int dlTup, int netDlTup) {
        this.mSceneId = sceneId;
        this.mVideoQoe = videoQoe;
        this.mUlTup = ulTup;
        this.mDlTup = dlTup;
        this.mNetDlTup = netDlTup;
    }
}
