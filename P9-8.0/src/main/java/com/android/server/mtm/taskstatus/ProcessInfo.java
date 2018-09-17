package com.android.server.mtm.taskstatus;

import java.util.ArrayList;

public class ProcessInfo {
    private static final int DEFAULT_CAPACITY = 10;
    public String mAdjType;
    public int mAppUid;
    public int mCount = 0;
    public long mCreatedTime = -1;
    public int mCurAdj = -1;
    public int mCurSchedGroup = 0;
    public boolean mForceToForeground = false;
    public boolean mForegroundActivities = false;
    public boolean mForegroundServices = false;
    public long mKilledTime = -1;
    public int mLru = -1;
    public ArrayList<String> mPackageName = new ArrayList(10);
    public int mPid;
    public String mProcessName;
    public int mType = 0;
    public int mUid;

    public ProcessInfo(int pid, int uid) {
        this.mPid = pid;
        this.mUid = uid;
    }

    public void initialProcessInfo(int pid, int uid) {
        this.mPid = pid;
        this.mUid = uid;
        this.mCount = 0;
        this.mCurSchedGroup = 0;
        this.mCurAdj = -1;
        this.mLru = -1;
        this.mKilledTime = -1;
        this.mCreatedTime = -1;
        this.mType = 0;
        this.mForegroundActivities = false;
        this.mForegroundServices = false;
        this.mForceToForeground = false;
        this.mPackageName = new ArrayList(10);
    }

    public static boolean copyProcessInfo(ProcessInfo source, ProcessInfo target) {
        if (source == null || target == null) {
            return false;
        }
        target.mPid = source.mPid;
        target.mUid = source.mUid;
        target.mAppUid = source.mAppUid;
        target.mCurSchedGroup = source.mCurSchedGroup;
        target.mCurAdj = source.mCurAdj;
        target.mLru = source.mLru;
        target.mType = source.mType;
        target.mForegroundActivities = source.mForegroundActivities;
        target.mForegroundServices = source.mForegroundServices;
        target.mForceToForeground = source.mForceToForeground;
        target.mProcessName = source.mProcessName;
        target.mAdjType = source.mAdjType;
        target.mPackageName.clear();
        target.mCreatedTime = source.mCreatedTime;
        int listSize = source.mPackageName.size();
        ArrayList<String> sourcePackageName = source.mPackageName;
        ArrayList<String> targetPackageName = target.mPackageName;
        for (int i = 0; i < listSize; i++) {
            targetPackageName.add((String) sourcePackageName.get(i));
        }
        return true;
    }
}
