package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;
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
    private static final boolean IS_DEDUG_LOCAL_CLASS = false;
    private static final String TAG = "SmartTrimProcessPkgResume";
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

    public SmartTrimProcessPkgResume(String pkgName, String processName) {
        super(1);
        this.mPkgName = pkgName;
        this.mProcessName = processName;
    }

    SmartTrimProcessPkgResume(StringTokenizer stzer) {
        super(1);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.mProcessName, this.mPkgName);
    }

    @Override // java.lang.Object
    public boolean equals(Object target) {
        if (target == null || !(target instanceof SmartTrimProcessPkgResume)) {
            return false;
        }
        SmartTrimProcessPkgResume input = (SmartTrimProcessPkgResume) target;
        if (Objects.equals(input.mProcessName, this.mProcessName) && Objects.equals(input.mPkgName, this.mPkgName)) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuffer sb = new StringBuffer("SmartTrimProcessPkgResume:" + System.lineSeparator());
        sb.append("process: " + this.mProcessName + System.lineSeparator());
        sb.append("pkg: " + this.mPkgName + System.lineSeparator());
        return sb.toString();
    }

    @Override // com.huawei.android.smcs.SmartTrimProcessEvent, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mProcessName);
        dest.writeString(this.mPkgName);
    }

    @Override // com.huawei.android.smcs.SmartTrimProcessEvent, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.android.smcs.SmartTrimProcessEvent
    public void readFromParcel(Parcel source) {
        this.mProcessName = source.readString();
        this.mPkgName = source.readString();
    }
}
