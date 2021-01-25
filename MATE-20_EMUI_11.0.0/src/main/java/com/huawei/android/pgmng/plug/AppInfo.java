package com.huawei.android.pgmng.plug;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.List;

public class AppInfo implements Parcelable {
    public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
        /* class com.huawei.android.pgmng.plug.AppInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
    private static final int PIDS_MAX_SIZE = 1024;
    private static final String TAG = "AppInfo";
    private List<Integer> mPids = null;
    private String mPkg = null;
    private int mUid = -1;

    public AppInfo(int uid, String pkg) {
        this.mUid = uid;
        this.mPkg = pkg;
        this.mPids = null;
    }

    public AppInfo(Parcel parcel) {
        this.mUid = parcel.readInt();
        this.mPkg = parcel.readString();
        int size = parcel.readInt();
        if (size > 1024 || size < 0) {
            Log.i(TAG, "getPid size is bad, size: " + size);
            this.mPids = null;
            return;
        }
        this.mPids = parcel.readArrayList(Integer.class.getClassLoader());
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUid);
        dest.writeString(this.mPkg);
        List<Integer> list = this.mPids;
        if (list == null) {
            dest.writeInt(-1);
            return;
        }
        dest.writeInt(list.size());
        dest.writeList(this.mPids);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void setUid(int uid) {
        this.mUid = uid;
    }

    public void setPkg(String pkg) {
        this.mPkg = pkg;
    }

    public void setPids(List<Integer> pids) {
        this.mPids = pids;
    }

    public int getUid() {
        return this.mUid;
    }

    public String getPkg() {
        return this.mPkg;
    }

    public List<Integer> getPids() {
        return this.mPids;
    }
}
