package com.android.server.hidata.appqoe;

public class HwAppQoeGameConfig {
    public int mGameAction = -1;
    public int mGameId = -1;
    public int mGameKqi = -1;
    public String mGameName = HwAppQoeUtils.INVALID_STRING_VALUE;
    public int mGameRtt = -1;
    private int mGameSpecialInfoSources = 0;
    public float mHistoryQoeBadTh = -1.0f;
    public String mReserved = HwAppQoeUtils.INVALID_STRING_VALUE;
    public int mScenesId = -1;

    public int getGameSpecialInfoSources() {
        return this.mGameSpecialInfoSources;
    }

    public void setGameSpecialInfoSources(int gameSpecialInfoSources) {
        this.mGameSpecialInfoSources = gameSpecialInfoSources;
    }

    public String toString() {
        return "HwAPPQoEGameConfig mGameName = " + this.mGameName + " mGameId = " + this.mGameId + " mGameId = " + this.mGameId + " mGameRtt = " + this.mGameRtt + " mGameKQI = " + this.mGameKqi + " mGameAction = " + this.mGameAction + " mHistoryQoeBadTH = " + this.mHistoryQoeBadTh + " mReserved = " + this.mReserved;
    }
}
