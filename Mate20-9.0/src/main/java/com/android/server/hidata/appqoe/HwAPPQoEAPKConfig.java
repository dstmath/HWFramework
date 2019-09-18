package com.android.server.hidata.appqoe;

public class HwAPPQoEAPKConfig {
    public String className = HwAPPQoEUtils.INVALID_STRING_VALUE;
    public int mAction = -1;
    public int mAggressiveStallTH = -1;
    public int mAppAlgorithm = -1;
    public int mAppId = -1;
    public int mAppPeriod = -1;
    public int mGeneralStallTH = -1;
    public float mHistoryQoeBadTH = -1.0f;
    public int mPlayActivity = -1;
    public int mQci = -1;
    public String mReserved = HwAPPQoEUtils.INVALID_STRING_VALUE;
    public int mScenceId = -1;
    public int mScenceType = 0;
    public int monitorUserLearning = -1;
    public String packageName = HwAPPQoEUtils.INVALID_STRING_VALUE;

    public String toString() {
        return " HwAPPQoEAPKConfig packageName = " + this.packageName + " className = " + this.className + " mAppId = " + this.mAppId + " mScenceId = " + this.mScenceId + " mScenceType = " + this.mScenceType + " mAppPeriod = " + this.mAppPeriod + " mAppAlgorithm = " + this.mAppAlgorithm + " mQci = " + this.mQci + " monitorUserLearning = " + this.monitorUserLearning + " mAction = " + this.mAction + " mHistoryQoeBadTH = " + this.mHistoryQoeBadTH + " mGeneralStallTH = " + this.mGeneralStallTH + " mAggressiveStallTH = " + this.mAggressiveStallTH + " mPlayActivity = " + this.mPlayActivity + " mReserved = " + this.mReserved;
    }
}
