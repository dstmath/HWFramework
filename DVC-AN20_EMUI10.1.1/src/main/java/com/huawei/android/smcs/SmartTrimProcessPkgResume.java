package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.StringTokenizer;

public final class SmartTrimProcessPkgResume extends SmartTrimProcessEvent {
    public static final Parcelable.Creator<SmartTrimProcessPkgResume> CREATOR = new Parcelable.Creator<SmartTrimProcessPkgResume>() {
        /* class com.huawei.android.smcs.SmartTrimProcessPkgResume.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SmartTrimProcessPkgResume createFromParcel(Parcel source) {
            return new SmartTrimProcessPkgResume(source);
        }

        @Override // android.os.Parcelable.Creator
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
            Log.e(TAG, "SmartTrimProcessPkgResume.hashCode: catch exception.");
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
            Log.e(TAG, "SmartTrimProcessPkgResume.equals: catch exception.");
            return false;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SmartTrimProcessPkgResume:\n");
        sb.append("process: " + this.mProcessName + "\n");
        sb.append("pkg: " + this.mPkgName + "\n");
        return sb.toString();
    }

    @Override // com.huawei.android.smcs.SmartTrimProcessEvent
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mProcessName);
        dest.writeString(this.mPkgName);
    }

    @Override // com.huawei.android.smcs.SmartTrimProcessEvent
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.android.smcs.SmartTrimProcessEvent
    public void readFromParcel(Parcel source) {
        this.mProcessName = source.readString();
        this.mPkgName = source.readString();
    }
}
