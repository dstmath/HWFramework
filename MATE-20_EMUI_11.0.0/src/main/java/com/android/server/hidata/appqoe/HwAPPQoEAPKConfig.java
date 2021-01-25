package com.android.server.hidata.appqoe;

public class HwAPPQoEAPKConfig {
    public String className = HwAPPQoEUtils.INVALID_STRING_VALUE;
    public int mAction = -1;
    public int mAggressiveStallTH = -1;
    public int mAppAlgorithm = -1;
    public int mAppId = -1;
    public int mAppPeriod = -1;
    private int mAppRegion = 0;
    private int mBadContinuousCnt = -1;
    private int mBadCount = -1;
    private int mChLoad2gTh = -1;
    private int mChLoad5gTh = -1;
    private int mDetectCycle = -1;
    public int mGeneralStallTH = -1;
    private int mGoodCount = -1;
    public float mHistoryQoeBadTH = -1.0f;
    private int mLinkSpeed2gTh = -1;
    private int mLinkSpeed5gTh = -1;
    private int mNoise2gTh = -1;
    private int mNoise5gTh = -1;
    private float mOtaRateTh = -1.0f;
    private int mPlayActivity = -1;
    public int mQci = -1;
    public String mReserved = HwAPPQoEUtils.INVALID_STRING_VALUE;
    private float mRtt = -1.0f;
    public int mScenceId = -1;
    public int mScenceType = 0;
    private int mSwitchType = -1;
    private float mTcpResendRate = -1.0f;
    private float mTcpRttTh = -1.0f;
    private int mThreshold = -1;
    private float mTxBadTh = -1.0f;
    private float mTxGoodTh = -1.0f;
    private int mWlanPlus = -1;
    public int monitorUserLearning = -1;
    public String packageName = HwAPPQoEUtils.INVALID_STRING_VALUE;

    public int getAppRegion() {
        return this.mAppRegion;
    }

    public void setAppRegion(int appRegion) {
        this.mAppRegion = appRegion;
    }

    public String toString() {
        return " HwAPPQoEAPKConfig packageName = " + this.packageName + " className = " + this.className + " mAppId = " + this.mAppId + " mScenceId = " + this.mScenceId + " mScenceType = " + this.mScenceType + " mAppPeriod = " + this.mAppPeriod + " mAppAlgorithm = " + this.mAppAlgorithm + " mQci = " + this.mQci + " monitorUserLearning = " + this.monitorUserLearning + " mAction = " + this.mAction + " mHistoryQoeBadTH = " + this.mHistoryQoeBadTH + " mGeneralStallTH = " + this.mGeneralStallTH + " mAggressiveStallTH = " + this.mAggressiveStallTH + " mPlayActivity = " + this.mPlayActivity + " mWlanPlus = " + this.mWlanPlus + " mSwitchType = " + this.mSwitchType + " mRtt = " + this.mRtt + " mThreshold = " + this.mThreshold + " mBadCount = " + this.mBadCount + " mGoodCount = " + this.mGoodCount + " mBadContinuousCnt = " + this.mBadContinuousCnt + " mTcpResendRate = " + this.mTcpResendRate + " mDetectCycle = " + this.mDetectCycle + " mNoise2gTh = " + this.mNoise2gTh + " mNoise5gTh = " + this.mNoise5gTh + " mLinkSpeed2gTh = " + this.mLinkSpeed2gTh + " mLinkSpeed5gTh = " + this.mLinkSpeed5gTh + " mChLoad2gTh = " + this.mChLoad2gTh + " mChLoad5gTh = " + this.mChLoad5gTh + " mOtaRateTh = " + this.mOtaRateTh + " mTxGoodTh " + this.mTxGoodTh + " mTxBadTh = " + this.mTxBadTh + " mTcpRttTh " + this.mTcpRttTh + " mReserved = " + this.mReserved;
    }

    public void setPlayActivity(int palyActivity) {
        this.mPlayActivity = palyActivity;
    }

    public int getPlayActivity() {
        return this.mPlayActivity;
    }

    public void setWlanPlus(int wlanPlus) {
        this.mWlanPlus = wlanPlus;
    }

    public int getWlanPlus() {
        return this.mWlanPlus;
    }

    public void setSwitchType(int switchType) {
        this.mSwitchType = switchType;
    }

    public int getSwitchType() {
        return this.mSwitchType;
    }

    public void setRtt(float rtt) {
        this.mRtt = rtt;
    }

    public float getRtt() {
        return this.mRtt;
    }

    public void setThreshlod(int threshold) {
        this.mThreshold = threshold;
    }

    public int getThreshold() {
        return this.mThreshold;
    }

    public void setBadCount(int badCount) {
        this.mBadCount = badCount;
    }

    public int getBadCount() {
        return this.mBadCount;
    }

    public void setGoodCount(int goodCount) {
        this.mGoodCount = goodCount;
    }

    public int getGoodCount() {
        return this.mGoodCount;
    }

    public void setBadContinuousCnt(int badContinuousCnt) {
        this.mBadContinuousCnt = badContinuousCnt;
    }

    public int getBadContinuousCnt() {
        return this.mBadContinuousCnt;
    }

    public void setTcpResendRate(float tcpResendRate) {
        this.mTcpResendRate = tcpResendRate;
    }

    public float getTcpResendRate() {
        return this.mTcpResendRate;
    }

    public void setDetectCycle(int detectCycle) {
        this.mDetectCycle = detectCycle;
    }

    public int getDetectCycle() {
        return this.mDetectCycle;
    }

    public void setNoise2gTh(int noiseTh) {
        this.mNoise2gTh = noiseTh;
    }

    public int getNoise2gTh() {
        return this.mNoise2gTh;
    }

    public void setNoise5gTh(int noiseTh) {
        this.mNoise5gTh = noiseTh;
    }

    public int getNoise5gTh() {
        return this.mNoise5gTh;
    }

    public void setLinkSpeed2gTh(int linkSpeed) {
        this.mLinkSpeed2gTh = linkSpeed;
    }

    public int getLinkSpeed2gTh() {
        return this.mLinkSpeed2gTh;
    }

    public void setLinkSpeed5gTh(int linkSpeed) {
        this.mLinkSpeed5gTh = linkSpeed;
    }

    public int getLinkSpeed5gTh() {
        return this.mLinkSpeed5gTh;
    }

    public void setChLoad2gTh(int chLoad) {
        this.mChLoad2gTh = chLoad;
    }

    public int getChLoad2gTh() {
        return this.mChLoad2gTh;
    }

    public void setChLoad5gTh(int chLoad) {
        this.mChLoad5gTh = chLoad;
    }

    public int getChLoad5gTh() {
        return this.mChLoad5gTh;
    }

    public void setOtaRateTh(float otaRate) {
        this.mOtaRateTh = otaRate;
    }

    public float getOtaRateTh() {
        return this.mOtaRateTh;
    }

    public void setTxGoodTh(float txGood) {
        this.mTxGoodTh = txGood;
    }

    public float getTxGoodTh() {
        return this.mTxGoodTh;
    }

    public void setTxBadTh(float txBad) {
        this.mTxBadTh = txBad;
    }

    public float getTxBadTh() {
        return this.mTxBadTh;
    }

    public void setTcpRttTh(float tcpRtt) {
        this.mTcpRttTh = tcpRtt;
    }

    public float getTcpRttTh() {
        return this.mTcpRttTh;
    }
}
