package com.huawei.pgmng.plug;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.List;

public class AppInfo implements Parcelable {
    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
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
        this.mPids = parcel.readArrayList(Integer.class.getClassLoader());
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUid);
        dest.writeString(this.mPkg);
        dest.writeList(this.mPids);
    }

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
