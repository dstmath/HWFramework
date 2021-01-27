package com.android.server.wifi.wificond;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public class RadioChainInfo implements Parcelable {
    public static final Parcelable.Creator<RadioChainInfo> CREATOR = new Parcelable.Creator<RadioChainInfo>() {
        /* class com.android.server.wifi.wificond.RadioChainInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RadioChainInfo createFromParcel(Parcel in) {
            RadioChainInfo result = new RadioChainInfo();
            result.chainId = in.readInt();
            result.level = in.readInt();
            return result;
        }

        @Override // android.os.Parcelable.Creator
        public RadioChainInfo[] newArray(int size) {
            return new RadioChainInfo[size];
        }
    };
    private static final String TAG = "RadioChainInfo";
    public int chainId;
    public int level;

    public RadioChainInfo() {
    }

    public RadioChainInfo(int chainId2, int level2) {
        this.chainId = chainId2;
        this.level = level2;
    }

    @Override // java.lang.Object
    public boolean equals(Object rhs) {
        RadioChainInfo chainInfo;
        if (this == rhs) {
            return true;
        }
        if (!(rhs instanceof RadioChainInfo) || (chainInfo = (RadioChainInfo) rhs) == null) {
            return false;
        }
        if (this.chainId == chainInfo.chainId && this.level == chainInfo.level) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.chainId), Integer.valueOf(this.level));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.chainId);
        out.writeInt(this.level);
    }
}
