package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashSet;
import java.util.Objects;
import java.util.StringTokenizer;

public final class SmartTrimProcessAddRelation extends SmartTrimProcessEvent {
    public static final Parcelable.Creator<SmartTrimProcessAddRelation> CREATOR = new Parcelable.Creator<SmartTrimProcessAddRelation>() {
        /* class com.huawei.android.smcs.SmartTrimProcessAddRelation.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SmartTrimProcessAddRelation createFromParcel(Parcel source) {
            return new SmartTrimProcessAddRelation(source);
        }

        @Override // android.os.Parcelable.Creator
        public SmartTrimProcessAddRelation[] newArray(int size) {
            return new SmartTrimProcessAddRelation[size];
        }
    };
    private static final boolean IS_DEBUG_LOCAL_CLASS = false;
    private static final String TAG = "SmartTrimProcessAddRelation";
    public HashSet<String> mClientPkgList = null;
    public String mClientProc;
    public HashSet<String> mServerPkgList = null;
    public String mServerProc;

    public SmartTrimProcessAddRelation(String clientProc, HashSet<String> clientPkgList, String serverProc, HashSet<String> serverPkgList) {
        super(0);
        this.mClientProc = clientProc;
        this.mClientPkgList = clientPkgList;
        this.mServerProc = serverProc;
        this.mServerPkgList = serverPkgList;
    }

    SmartTrimProcessAddRelation(Parcel source) {
        super(source);
        readFromParcel(source);
    }

    SmartTrimProcessAddRelation(Parcel source, int event) {
        super(event);
        readFromParcel(source);
    }

    SmartTrimProcessAddRelation(StringTokenizer stzer) {
        super(0);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.mClientProc, this.mServerProc);
    }

    @Override // java.lang.Object
    public boolean equals(Object target) {
        if (target == null || !(target instanceof SmartTrimProcessAddRelation)) {
            return false;
        }
        SmartTrimProcessAddRelation input = (SmartTrimProcessAddRelation) target;
        boolean isClientEqual = Objects.equals(input.mClientProc, this.mClientProc);
        boolean isServerEqual = Objects.equals(input.mServerProc, this.mServerProc);
        if (!isClientEqual || !isServerEqual) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuffer sb = new StringBuffer("SmartTrimProcessAddRelation:" + System.lineSeparator());
        sb.append("client process: " + this.mClientProc + System.lineSeparator());
        sb.append("client pkg list: " + this.mClientPkgList + System.lineSeparator());
        sb.append("server process: " + this.mServerProc + System.lineSeparator());
        sb.append("server pkg list: " + this.mServerPkgList + System.lineSeparator());
        return sb.toString();
    }

    @Override // com.huawei.android.smcs.SmartTrimProcessEvent, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mClientProc);
        dest.writeStringArray(hashSet2strings(this.mClientPkgList));
        dest.writeString(this.mServerProc);
        dest.writeStringArray(hashSet2strings(this.mServerPkgList));
    }

    @Override // com.huawei.android.smcs.SmartTrimProcessEvent, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.android.smcs.SmartTrimProcessEvent
    public void readFromParcel(Parcel source) {
        this.mClientProc = source.readString();
        this.mClientPkgList = strings2hashSet(source.readStringArray());
        this.mServerProc = source.readString();
        this.mServerPkgList = strings2hashSet(source.readStringArray());
    }
}
