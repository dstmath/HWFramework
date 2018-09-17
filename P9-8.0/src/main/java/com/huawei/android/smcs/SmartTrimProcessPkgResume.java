package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.StringTokenizer;

public final class SmartTrimProcessPkgResume extends SmartTrimProcessEvent {
    public static final Creator<SmartTrimProcessPkgResume> CREATOR = new Creator<SmartTrimProcessPkgResume>() {
        public SmartTrimProcessPkgResume createFromParcel(Parcel source) {
            return new SmartTrimProcessPkgResume(source);
        }

        public SmartTrimProcessPkgResume[] newArray(int size) {
            return new SmartTrimProcessPkgResume[size];
        }
    };
    private static final String TAG = "SmartTrimProcessPkgResume";
    private static final boolean mDebugLocalClass = false;
    public String mPkgName = null;
    public String mProcessName = null;

    SmartTrimProcessPkgResume(Parcel source) {
        super(source);
        readFromParcel(source);
    }

    SmartTrimProcessPkgResume(Parcel source, int event) {
        super(event);
        readFromParcel(source);
    }

    public SmartTrimProcessPkgResume(String sPkg, String processName) {
        super(1);
        this.mPkgName = sPkg;
        this.mProcessName = processName;
    }

    SmartTrimProcessPkgResume(StringTokenizer stzer) {
        super(1);
    }

    public int hashCode() {
        try {
            String sHashCode = this.mProcessName + "_" + this.mPkgName;
            if (sHashCode == null || sHashCode.length() <= 0) {
                return -1;
            }
            return sHashCode.hashCode();
        } catch (Exception e) {
            Log.e(TAG, "SmartTrimProcessPkgResume.hashCode: catch exception " + e.toString());
            return -1;
        }
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        try {
            if (!(o instanceof SmartTrimProcessPkgResume)) {
                return false;
            }
            SmartTrimProcessPkgResume input = (SmartTrimProcessPkgResume) o;
            if (input.mProcessName.equals(this.mProcessName) && input.mPkgName.equals(this.mPkgName)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "SmartTrimProcessPkgResume.equals: catch exception " + e.toString());
            return false;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SmartTrimProcessPkgResume:\n");
        sb.append("process: " + this.mProcessName + "\n");
        sb.append("pkg: " + this.mPkgName + "\n");
        return sb.toString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mProcessName);
        dest.writeString(this.mPkgName);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.mProcessName = source.readString();
        this.mPkgName = source.readString();
    }
}
