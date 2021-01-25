package com.huawei.netassistant.common;

import android.os.Parcel;
import android.os.Parcelable;

public class SimCardSettingsInfo implements Parcelable {
    public static final Parcelable.Creator<SimCardSettingsInfo> CREATOR = new Parcelable.Creator<SimCardSettingsInfo>() {
        /* class com.huawei.netassistant.common.SimCardSettingsInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SimCardSettingsInfo createFromParcel(Parcel in) {
            return new SimCardSettingsInfo();
        }

        @Override // android.os.Parcelable.Creator
        public SimCardSettingsInfo[] newArray(int size) {
            return new SimCardSettingsInfo[size];
        }
    };

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
