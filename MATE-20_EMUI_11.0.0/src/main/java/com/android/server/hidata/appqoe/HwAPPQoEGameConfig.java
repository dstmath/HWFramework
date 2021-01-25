package com.android.server.hidata.appqoe;

public class HwAPPQoEGameConfig {
    public int mGameAction = -1;
    public int mGameId = -1;
    public int mGameKQI = -1;
    public String mGameName = HwAPPQoEUtils.INVALID_STRING_VALUE;
    public int mGameRtt = -1;
    private int mGameSpecialInfoSources = 0;
    public float mHistoryQoeBadTH = -1.0f;
    public String mReserved = HwAPPQoEUtils.INVALID_STRING_VALUE;
    public int mScenceId = -1;

    public int getGameSpecialInfoSources() {
        return this.mGameSpecialInfoSources;
    }

    public void setGameSpecialInfoSources(int gameSpecialInfoSources) {
        this.mGameSpecialInfoSources = gameSpecialInfoSources;
    }

    public String toString() {
        return " HwAPPQoEGameConfig mGameName = " + this.mGameName + " mGameId = " + this.mGameId + " mGameId = " + this.mGameId + " mGameRtt = " + this.mGameRtt + " mGameKQI = " + this.mGameKQI + " mGameAction = " + this.mGameAction + " mHistoryQoeBadTH = " + this.mHistoryQoeBadTH + " mReserved = " + this.mReserved;
    }
}
