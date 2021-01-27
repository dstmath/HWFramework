package com.huawei.systemmanager.sdk.tmsdk.netassistant;

import android.os.Parcel;
import android.os.Parcelable;

public class SimProfileDes implements Parcelable {
    public static final Parcelable.Creator<SimProfileDes> CREATOR = new Parcelable.Creator<SimProfileDes>() {
        /* class com.huawei.systemmanager.sdk.tmsdk.netassistant.SimProfileDes.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SimProfileDes createFromParcel(Parcel in) {
            return new SimProfileDes();
        }

        @Override // android.os.Parcelable.Creator
        public SimProfileDes[] newArray(int size) {
            return new SimProfileDes[size];
        }
    };

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
    }
}
