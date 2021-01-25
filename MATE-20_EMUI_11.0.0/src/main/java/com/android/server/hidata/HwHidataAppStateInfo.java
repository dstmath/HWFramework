package com.android.server.hidata;

public class HwHidataAppStateInfo {
    public static final int GAME_SCENCE_IN_WAR = 200002;
    public static final int GAME_SCENCE_NOT_IN_WAR = 200001;
    public static final int MSG_APP_STATE_END = 101;
    public static final int MSG_APP_STATE_START = 100;
    public static final int MSG_APP_STATE_UPDATE = 102;
    private int mAction;
    private int mAppId;
    private int mCurRtt;
    private int mCurScence;
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

    public int getCurScence() {
        return this.mCurScence;
    }

    public void setCurScence(int scence) {
        this.mCurScence = scence;
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
