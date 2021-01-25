package com.android.server.mtm.taskstatus;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class ProcessInfo implements Parcelable {
    public static final Parcelable.Creator<ProcessInfo> CREATOR = new Parcelable.Creator<ProcessInfo>() {
        /* class com.android.server.mtm.taskstatus.ProcessInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ProcessInfo createFromParcel(Parcel source) {
            return new ProcessInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public ProcessInfo[] newArray(int size) {
            return new ProcessInfo[size];
        }
    };
    private static final int DEFAULT_CAPACITY = 10;
    public String mAdjType;
    public int mAppUid;
    public int mCount;
    public long mCreatedTime;
    public int mCurAdj;
    public int mCurSchedGroup;
    public boolean mForceToForeground;
    public boolean mForegroundActivities;
    public boolean mForegroundServices;
    public long mKilledTime;
    public int mLru;
    public ArrayList<String> mPackageName;
    public int mPid;
    public String mProcessName;
    public int mSetProcState;
    public int mTargetSdkVersion;
    public int mType;
    public int mUid;

    public ProcessInfo(int pid, int uid) {
        this.mPid = pid;
        this.mUid = uid;
        this.mAppUid = -1;
        this.mTargetSdkVersion = -1;
        this.mCount = 0;
        this.mCurSchedGroup = 0;
        this.mCurAdj = -1;
        this.mLru = -1;
        this.mSetProcState = -1;
        this.mKilledTime = -1;
        this.mCreatedTime = -1;
        this.mType = 0;
        this.mForegroundActivities = false;
        this.mForegroundServices = false;
        this.mForceToForeground = false;
        this.mPackageName = new ArrayList<>(10);
    }

    public void initialProcessInfo(int pid, int uid) {
        this.mPid = pid;
        this.mUid = uid;
        this.mAppUid = -1;
        this.mTargetSdkVersion = -1;
        this.mCount = 0;
        this.mCurSchedGroup = 0;
        this.mCurAdj = -1;
        this.mLru = -1;
        this.mSetProcState = -1;
        this.mKilledTime = -1;
        this.mCreatedTime = -1;
        this.mType = 0;
        this.mForegroundActivities = false;
        this.mForegroundServices = false;
        this.mForceToForeground = false;
        this.mPackageName = new ArrayList<>(10);
    }

    public static boolean copyProcessInfo(ProcessInfo source, ProcessInfo target) {
        if (source == null || target == null) {
            return false;
        }
        target.mPid = source.mPid;
        target.mUid = source.mUid;
        target.mAppUid = source.mAppUid;
        target.mTargetSdkVersion = source.mTargetSdkVersion;
        target.mCurSchedGroup = source.mCurSchedGroup;
        target.mCurAdj = source.mCurAdj;
        target.mLru = source.mLru;
        target.mSetProcState = source.mSetProcState;
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
            targetPackageName.add(sourcePackageName.get(i));
        }
        return true;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPid);
        dest.writeInt(this.mUid);
        dest.writeInt(this.mAppUid);
        dest.writeInt(this.mTargetSdkVersion);
        dest.writeInt(this.mCount);
        dest.writeInt(this.mCurSchedGroup);
        dest.writeInt(this.mCurAdj);
        dest.writeInt(this.mType);
        dest.writeInt(this.mLru);
        dest.writeInt(this.mSetProcState);
        dest.writeLong(this.mKilledTime);
        dest.writeLong(this.mCreatedTime);
        dest.writeByte(this.mForegroundActivities ? (byte) 1 : 0);
        dest.writeByte(this.mForegroundServices ? (byte) 1 : 0);
        dest.writeByte(this.mForceToForeground ? (byte) 1 : 0);
        dest.writeStringList(this.mPackageName);
        dest.writeString(this.mProcessName);
        dest.writeString(this.mAdjType);
    }

    protected ProcessInfo(Parcel in) {
        this.mPid = in.readInt();
        this.mUid = in.readInt();
        this.mAppUid = in.readInt();
        this.mTargetSdkVersion = in.readInt();
        this.mCount = in.readInt();
        this.mCurSchedGroup = in.readInt();
        this.mCurAdj = in.readInt();
        this.mType = in.readInt();
        this.mLru = in.readInt();
        this.mSetProcState = in.readInt();
        this.mKilledTime = in.readLong();
        this.mCreatedTime = in.readLong();
        boolean z = true;
        this.mForegroundActivities = in.readByte() != 0;
        this.mForegroundServices = in.readByte() != 0;
        this.mForceToForeground = in.readByte() == 0 ? false : z;
        this.mPackageName = in.createStringArrayList();
        this.mProcessName = in.readString();
        this.mAdjType = in.readString();
    }
}
