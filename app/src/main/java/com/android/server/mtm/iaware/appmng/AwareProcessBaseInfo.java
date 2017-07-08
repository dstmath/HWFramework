package com.android.server.mtm.iaware.appmng;

import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;

public class AwareProcessBaseInfo {
    public String mAdjType;
    public int mCurAdj;
    public boolean mForegroundActivities;
    public boolean mHasShownUi;
    public int mUid;

    public AwareProcessBaseInfo() {
        this.mAdjType = AppHibernateCst.INVALID_PKG;
        this.mCurAdj = 0;
        this.mForegroundActivities = false;
        this.mAdjType = AppHibernateCst.INVALID_PKG;
    }

    public AwareProcessBaseInfo copy() {
        AwareProcessBaseInfo dst = new AwareProcessBaseInfo();
        dst.mUid = this.mUid;
        dst.mCurAdj = this.mCurAdj;
        dst.mAdjType = this.mAdjType;
        dst.mForegroundActivities = this.mForegroundActivities;
        dst.mHasShownUi = this.mHasShownUi;
        return dst;
    }
}
