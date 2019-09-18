package com.android.server.mtm.iaware.appmng;

public class AwareProcessBaseInfo {
    public String mAdjType = "";
    public int mAppUid;
    public int mCurAdj = 0;
    public boolean mForegroundActivities = false;
    public boolean mHasShownUi;
    public int mSetProcState;
    public int mUid;

    public AwareProcessBaseInfo copy() {
        AwareProcessBaseInfo dst = new AwareProcessBaseInfo();
        dst.mUid = this.mUid;
        dst.mAppUid = this.mAppUid;
        dst.mSetProcState = this.mSetProcState;
        dst.mCurAdj = this.mCurAdj;
        dst.mAdjType = this.mAdjType;
        dst.mForegroundActivities = this.mForegroundActivities;
        dst.mHasShownUi = this.mHasShownUi;
        return dst;
    }
}
