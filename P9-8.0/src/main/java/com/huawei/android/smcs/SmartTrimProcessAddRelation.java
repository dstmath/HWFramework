package com.huawei.android.smcs;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.HashSet;
import java.util.StringTokenizer;

public final class SmartTrimProcessAddRelation extends SmartTrimProcessEvent {
    public static final Creator<SmartTrimProcessAddRelation> CREATOR = new Creator<SmartTrimProcessAddRelation>() {
        public SmartTrimProcessAddRelation createFromParcel(Parcel source) {
            return new SmartTrimProcessAddRelation(source);
        }

        public SmartTrimProcessAddRelation[] newArray(int size) {
            return new SmartTrimProcessAddRelation[size];
        }
    };
    private static final String TAG = "SmartTrimProcessAddRelation";
    private static final boolean mDebugLocalClass = false;
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

    public int hashCode() {
        try {
            return (this.mClientProc + this.mServerProc).hashCode();
        } catch (Exception e) {
            Log.e(TAG, "SmartTrimProcessAddRelation.hashCode: catch exception " + e.toString());
            return -1;
        }
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        try {
            if (!(o instanceof SmartTrimProcessAddRelation)) {
                return false;
            }
            SmartTrimProcessAddRelation input = (SmartTrimProcessAddRelation) o;
            boolean clientEqual = input.mClientProc.equals(this.mClientProc);
            boolean serverEqual = input.mServerProc.equals(this.mServerProc);
            if (!clientEqual) {
                serverEqual = false;
            }
            if (serverEqual) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "SmartTrimProcessAddRelation.equals: catch exception " + e.toString());
            return false;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SmartTrimProcessAddRelation:\n");
        sb.append("client process: " + this.mClientProc + "\n");
        sb.append("client pkg list: " + this.mClientPkgList + "\n");
        sb.append("server process: " + this.mServerProc + "\n");
        sb.append("server pkg list: " + this.mServerPkgList + "\n");
        return sb.toString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mClientProc);
        dest.writeStringArray(hashSet2strings(this.mClientPkgList));
        dest.writeString(this.mServerProc);
        dest.writeStringArray(hashSet2strings(this.mServerPkgList));
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel source) {
        this.mClientProc = source.readString();
        this.mClientPkgList = strings2hashSet(source.readStringArray());
        this.mServerProc = source.readString();
        this.mServerPkgList = strings2hashSet(source.readStringArray());
    }
}
