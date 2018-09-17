package com.android.server.rms.iaware.appmng;

public final class AwareAppStartStatusCache {
    public static final int APPTYPE_DEFAULT = -2;
    public String mAction;
    public int mAppType = -2;
    public int mCallerUid;
    public String mCompName;
    public int mFlags;
    public boolean mIsAppStop;
    public boolean mIsBtMediaBrowserCaller;
    public boolean mIsCallerFg;
    public boolean mIsSystemApp;
    public boolean mIsTargetFg;
    public boolean mNotifyListenerCaller;
    public int mUid;
    public int unPercetibleAlarm;

    public AwareAppStartStatusCache(int uid, int callerUid, boolean isSystem, boolean isAppStop, String compName, String action, boolean isCallerFg, boolean isTargetFg, int flags, boolean isBtMediaBrowserCaller, boolean notifyListenerCaller, int unPercetible) {
        this.mUid = uid;
        this.mCallerUid = callerUid;
        this.mIsSystemApp = isSystem;
        this.mIsAppStop = isAppStop;
        this.mCompName = compName;
        this.mAction = action;
        this.mIsCallerFg = isCallerFg;
        this.mIsTargetFg = isTargetFg;
        this.mFlags = flags;
        this.mIsBtMediaBrowserCaller = isBtMediaBrowserCaller;
        this.unPercetibleAlarm = unPercetible;
        this.mNotifyListenerCaller = notifyListenerCaller;
    }
}
