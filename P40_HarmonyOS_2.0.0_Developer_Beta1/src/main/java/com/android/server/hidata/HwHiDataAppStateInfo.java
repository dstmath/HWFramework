package com.android.server.hidata;

public class HwHiDataAppStateInfo {
    public static final int MSG_APP_STATE_END = 101;
    public static final int MSG_APP_STATE_START = 100;
    private int mAction;
    private int mAppId;
    private int mCurRtt;
    private int mCurScenes;
    private int mCurState;
    private int mCurUid;

    public int getAppId() {
        return this.mAppId;
    }

    public void setAppId(int appId) {
        this.mAppId = appId;
    }

    public int getCurUid() {
        return this.mCurUid;
    }

    public void setCurUid(int uid) {
        this.mCurUid = uid;
    }

    public int getCurState() {
        return this.mCurState;
    }

    public void setCurState(int state) {
        this.mCurState = state;
    }

    public int getCurScenes() {
        return this.mCurScenes;
    }

    public void setCurScenes(int scence) {
        this.mCurScenes = scence;
    }

    public int getCurRtt() {
        return this.mCurRtt;
    }

    public void setCurRtt(int rtt) {
        this.mCurRtt = rtt;
    }

    public int getAction() {
        return this.mAction;
    }

    public void setAction(int action) {
        this.mAction = action;
    }
}
